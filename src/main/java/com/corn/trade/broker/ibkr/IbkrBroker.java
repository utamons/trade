package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.service.AssetService;
import com.corn.trade.type.PositionType;
import com.corn.trade.type.TimeFrame;
import com.corn.trade.util.ChangeOrderListener;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Trigger;
import com.ib.client.*;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Bar;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class IbkrBroker extends Broker {
	private static final org.slf4j.Logger       log                 = org.slf4j.LoggerFactory.getLogger(IbkrBroker.class);
	private final        IbkrConnectionHandler  ibkrConnectionHandler;
	private final        IbkrOrderHelper        ibkrOrderHelper;
	private              ContractDetails        contractDetails;
	private              ITopMktDataHandler     mktDataHandler;
	private              IHistoricalDataHandler dayHighLowDataHandler;
	private              IHistoricalDataHandler adrDataHandler;
	private              boolean                requestedMarketData = false;

	public IbkrBroker(String ticker, String exchange, Trigger disconnectionListener) throws BrokerException {
		log.debug("init start");

		try {
			this.ibkrConnectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			this.ibkrConnectionHandler.setDisconnectionListener(disconnectionListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage(), e);
		}

		initHandlers();

		initContract(ticker, exchange);

		ibkrOrderHelper = new IbkrOrderHelper(ibkrConnectionHandler, this);

		log.debug("init finish");
	}

	private static String getLastDayEnd(Exchange exchange) {
		ExchangeTime exchangeTime = new ExchangeTime(exchange);

		return exchangeTime.lastTradingDayEnd().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
		       " " +
		       exchangeTime.endTrading().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
		       " " +
		       exchange.getTimeZone();
	}

	private void initContract(String ticker, String exchange) throws BrokerException {
		Contract contract = new Contract();
		contract.symbol(ticker);
		contract.secType("STK");
		contract.primaryExch(exchange);

		if (exchange.equals("NYSE") || exchange.equals("NASDAQ")) {
			contract.exchange("SMART");
			contract.currency("USD");
		} else contract.exchange(exchange);

		log.debug("looking up contract");
		List<ContractDetails> contractDetailsList = ibkrConnectionHandler.lookupContract(contract);
		if (contractDetailsList.isEmpty()) {
			throw new BrokerException("No contract details found for " + ticker);
		} else if (contractDetailsList.size() > 1) {
			throw new BrokerException("Multiple contract details found for " + ticker);
		}
		log.debug("contract details found");

		this.exchangeName = contractDetailsList.get(0).contract().primaryExch();
		contractDetails = contractDetailsList.get(0);
	}

	private void initHandlers() {
		mktDataHandler = new ApiController.TopMktDataAdapter() {
			@Override
			public void tickPrice(TickType tickType, double price, TickAttrib attribs) {
				if (tickType == TickType.ASK) {
					ask = price;
				}
				if (tickType == TickType.BID) {
					bid = price;
				}
				if (tickType == TickType.LAST) {
					IbkrBroker.this.price = price;
				}
				try {
					notifyTradeContext();
				} catch (BrokerException e) {
					log.error("{}", e.getMessage());
				}
			}
		};
		dayHighLowDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				dayHigh = bar.high();
				dayLow = bar.low();
			}

			@Override
			public void historicalDataEnd() {
				try {
					notifyTradeContext();
				} catch (BrokerException e) {
					log.error("{}", e.getMessage());
				}
			}
		};

		adrDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				com.corn.trade.model.Bar tradeBar = com.corn.trade.model.Bar.BarBuilder.aBar()
				                                                                       .withOpen(bar.open())
				                                                                       .withClose(bar.close())
				                                                                       .withHigh(bar.high())
				                                                                       .withLow(bar.low())
				                                                                       .withVolume(bar.volume().longValue())
				                                                                       .withTime(bar.time())
				                                                                       .withTimeFrame(TimeFrame.D1)
				                                                                       .build();
				adrBarList.add(tradeBar);
			}

			@Override
			public void historicalDataEnd() {
				try {
					notifyTradeContext();
				} catch (BrokerException e) {
					log.error("{}", e.getMessage());
				}
			}
		};
	}

	ContractDetails getContractDetails() {
		return contractDetails;
	}

	@Override
	protected synchronized void requestAdr() throws BrokerException {
		if (adr != null) {
			notifyTradeContext();
			return;
		}

		final String lastDayEnd;
		try {
			Exchange exchange = new AssetService().getExchange(exchangeName);
			lastDayEnd = getLastDayEnd(exchange);
		} catch (DBException e) {
			throw new BrokerException("DB error: ", e);
		}

		if (!ibkrConnectionHandler.isConnected()) throw new BrokerException("IBKR disconnected");

		ibkrConnectionHandler.controller()
		                     .reqHistoricalData(contractDetails.contract(),
		                                        lastDayEnd,
		                                        ADR_BARS,
		                                        Types.DurationUnit.DAY,
		                                        Types.BarSize._1_day,
		                                        Types.WhatToShow.TRADES,
		                                        true,
		                                        false,
		                                        adrDataHandler);
	}

	@Override
	protected synchronized void requestMarketData() throws BrokerException {
		if (requestedMarketData) return;
		if (!ibkrConnectionHandler.isConnected()) throw new BrokerException("IBKR disconnected");
		ibkrConnectionHandler.controller().reqTopMktData(contractDetails.contract(), "", false, false, mktDataHandler);
		ibkrConnectionHandler.controller()
		                     .reqHistoricalData(contractDetails.contract(),
		                                        "",
		                                        1,
		                                        Types.DurationUnit.DAY,
		                                        Types.BarSize._1_day,
		                                        Types.WhatToShow.TRADES,
		                                        true,
		                                        true,
		                                        dayHighLowDataHandler);
		requestedMarketData = true;
	}

	@Override
	protected void cancelMarketData() {
		ibkrConnectionHandler.controller().cancelTopMktData(mktDataHandler);
		ibkrConnectionHandler.controller().cancelHistoricalData(dayHighLowDataHandler);
		requestedMarketData = false;
	}

	@Override
	public OrderBracketIds placeOrderWithBracket(long qtt,
	                                             Double stop,
	                                             Double limit,
	                                             Double stopLoss,
	                                             Double takeProfit,
	                                             PositionType positionType,
	                                             com.corn.trade.type.OrderType tOrderType,
	                                             ChangeOrderListener mainOrderListener,
	                                             ChangeOrderListener tpOrderListener,
	                                             ChangeOrderListener slOrderListener) throws BrokerException {
		Action action = positionType == PositionType.LONG ? Action.BUY : Action.SELL;
		try {
			return ibkrOrderHelper.placeOrderWithBracket(contractDetails,
			                                             qtt,
			                                             stop,
			                                             limit,
			                                             stopLoss,
			                                             takeProfit,
			                                             action,
			                                             fromTOrderType(tOrderType),
			                                             mainOrderListener,
			                                             tpOrderListener,
			                                             slOrderListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage());
		}
	}

	public static OrderType fromTOrderType(com.corn.trade.type.OrderType tOrderType) {
		return switch (tOrderType) {
			case STP -> OrderType.STP;
			case LMT -> OrderType.LMT;
			case MKT -> OrderType.MKT;
			case STP_LMT -> OrderType.STP_LMT;
			case MOC -> OrderType.MOC;
		};
	}

	public static com.corn.trade.type.OrderStatus fromOrderStatus(OrderStatus status) {
		return switch (status) {
			case ApiPending, PreSubmitted, PendingSubmit, PendingCancel -> com.corn.trade.type.OrderStatus.PENDING;
			case ApiCancelled, Cancelled -> com.corn.trade.type.OrderStatus.CANCELLED;
			case Submitted -> com.corn.trade.type.OrderStatus.SUBMITTED;
			case Filled -> com.corn.trade.type.OrderStatus.FILLED;
			case Inactive -> com.corn.trade.type.OrderStatus.INACTIVE;
			case Unknown -> com.corn.trade.type.OrderStatus.UNKNOWN;
		};
	}
}
