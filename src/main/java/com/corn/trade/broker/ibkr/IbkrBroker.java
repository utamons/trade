/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.model.ExecutionData;
import com.corn.trade.model.Position;
import com.corn.trade.service.AssetService;
import com.corn.trade.type.ActionType;
import com.corn.trade.type.PositionType;
import com.corn.trade.type.TimeFrame;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class IbkrBroker extends Broker {
	private static final org.slf4j.Logger       log = org.slf4j.LoggerFactory.getLogger(IbkrBroker.class);
	private final        IbkrOrderHelper        ibkrOrderHelper;
	private final        IbkrPositionSubscriber positionSubscriber;
	private              IbkrPnLSubscriber      pnlSubscriber;

	private IbkrConnectionHandler  ibkrConnectionHandler;
	private ContractDetails        contractDetails;
	private ITopMktDataHandler     mktDataHandler;
	private IHistoricalDataHandler dayHighLowDataHandler;
	private IHistoricalDataHandler adrDataHandler;
	private boolean                requestedMarketData = false;

	public IbkrBroker(String ticker, String exchange, Trigger disconnectionListener) throws BrokerException {
		super(disconnectionListener);
		log.debug("init start");

		initHandlers();
		initContract(ticker, exchange);

		ibkrOrderHelper = new IbkrOrderHelper(ibkrConnectionHandler);
		positionSubscriber = IbkrSubscriberFactory.getPositionSubscriber();

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
				} catch (Exception e) {
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
	protected void initConnection(Trigger disconnectionListener) throws BrokerException {
		try {
			this.ibkrConnectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			this.ibkrConnectionHandler.setDisconnectionListener(disconnectionListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage(), e);
		}
	}

	@Override
	public int addPositionListener(Consumer<Position> positionListener) throws BrokerException {
		return positionSubscriber.addListener(contractDetails.contract().conid(), getAccount(), positionListener);
	}

	@Override
	public int addPnListener(Consumer<com.corn.trade.model.PnL> pnlListener) throws BrokerException {
		if (pnlSubscriber == null) {
			pnlSubscriber = IbkrSubscriberFactory.getPnlSubscriber();
		}
		return pnlSubscriber.addListener(getAccount(), pnlListener);
	}

	@Override
	public int addAccountListener(Consumer<com.corn.trade.model.AccountUpdate> accountListener) throws BrokerException {
		IbkrAccountSubscriber accountSubscriber = IbkrSubscriberFactory.getAccountSubscriber();
		return accountSubscriber.addListener(getAccount(), accountListener);
	}

	private String getAccount() throws BrokerException {
		List<String> accounts = ibkrConnectionHandler.getAccountList();
		if (accounts.size() != 1) {
			throw new BrokerException("Expected one account, got " + accounts.size());
		}
		return accounts.get(0);
	}

	@Override
	public void removePositionListener(int id) throws BrokerException {
		positionSubscriber.removeListener(contractDetails.contract().conid(), getAccount(), id);
	}

	@Override
	public void removeAllPositionListeners() throws BrokerException {
		positionSubscriber.removeAllListeners(contractDetails.contract().conid(), getAccount());
	}

	@Override
	protected void requestPositionUpdates() throws BrokerException {
		positionSubscriber.subscribe(assetName, contractDetails.contract().conid(), getAccount());
	}

	@Override
	public void requestPnLUpdates() throws BrokerException {
		if (pnlSubscriber == null) {
			pnlSubscriber = IbkrSubscriberFactory.getPnlSubscriber();
		}
		pnlSubscriber.subscribe(getAccount());
	}

	@Override
	public void requestAccountUpdates() throws BrokerException {
		IbkrAccountSubscriber accountSubscriber = IbkrSubscriberFactory.getAccountSubscriber();
		accountSubscriber.subscribe(getAccount());
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
	public void requestExecutionData(CompletableFuture<List<ExecutionData>> executions) throws BrokerException {
		ExecutionFilter filter = new ExecutionFilter();
		filter.symbol(contractDetails.contract().symbol());
		filter.secType(contractDetails.contract().secType().name());
		final ExchangeTime exchangeTime = getExchangeTime();

		ibkrConnectionHandler.controller().reqExecutions(filter, new ApiController.ITradeReportHandler() {
			final List<Execution> executionList = new java.util.ArrayList<>();

			@Override
			public void tradeReport(String tradeKey, Contract contract, Execution execution) {
				if (contract.conid() == contractDetails.contract().conid()) executionList.add(execution);
			}

			@Override
			public void tradeReportEnd() {
				log.debug("tradeReportEnd executionList size: {}", executionList.size());
				executions.complete(executionList.stream()
				                                 .map(execution -> new ExecutionData(String.valueOf(execution.orderId()),
				                                                                     getAssetName(),
				                                                                     exchangeTime.ibkrExecutionToLocalDateTime(execution.time()),
				                                                                     execution.price(),
				                                                                     execution.avgPrice(),
				                                                                     execution.shares().longValue(),
				                                                                     execution.side()))
				                                 .toList());
			}

			@Override
			public void commissionReport(String tradeKey, CommissionReport commissionReport) {
				log.debug("commission {}", commissionReport.commission());
			}
		});
	}

	@Override
	public OrderBracketIds placeOrderWithBracket(long qtt,
	                                             Double stop,
	                                             Double limit,
	                                             Double stopLoss,
	                                             Double takeProfit,
	                                             PositionType positionType,
	                                             com.corn.trade.type.OrderType tOrderType,
	                                             Consumer<com.corn.trade.type.OrderStatus> mainExecutionListener) throws BrokerException {
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
			                                             mainExecutionListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage());
		}
	}

	@Override
	public void placeOrder(long qtt,
	                       Double stop,
	                       Double limit,
	                       ActionType actionType,
	                       com.corn.trade.type.OrderType tOrderType,
	                       Consumer<com.corn.trade.type.OrderStatus> executionListener) throws BrokerException {
		Action action = actionType == ActionType.BUY ? Action.BUY : Action.SELL;
		try {
			ibkrOrderHelper.placeOrder(contractDetails,
			                           qtt,
			                           stop,
			                           limit,
			                           action,
			                           fromTOrderType(tOrderType),
			                           executionListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage());
		}
	}

	@Override
	public void cleanAllOrders() {
		ibkrOrderHelper.cleanAllOrdersForContract(contractDetails.contract());
	}

	@Override
	public void modifyStopLoss(long quantity, double stopLossPrice, ActionType actionType) {
		Action action = actionType == ActionType.BUY ? Action.BUY : Action.SELL;
		ibkrOrderHelper.modifyStopLoss(Integer.parseInt(bracketIds.stopLossId()),
		                               contractDetails.contract(),
		                               quantity,
		                               stopLossPrice,
		                               action);
	}

	@Override
	public void modifyTakeProfit(long quantity, double takeProfitPrice, ActionType actionType) {
		if (bracketIds.takeProfitId() == null) return;
		Action action = actionType == ActionType.BUY ? Action.BUY : Action.SELL;
		ibkrOrderHelper.modifyTakeProfit(Integer.parseInt(bracketIds.takeProfitId()),
		                                 contractDetails.contract(),
		                                 quantity,
		                                 takeProfitPrice,
		                                 action);
	}
}
