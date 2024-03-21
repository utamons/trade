package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.service.AssetService;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Trigger;
import com.ib.client.*;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Bar;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class IbkrBroker extends Broker {
	public static final  int              ADR_BARS = 14;
	private static final org.slf4j.Logger log      = org.slf4j.LoggerFactory.getLogger(IbkrBroker.class);
	private final IbkrAdapter            ibkrAdapter;
	private       ContractDetails        contractDetails;
	private       ITopMktDataHandler     mktDataHandler;
	private       IHistoricalDataHandler dayHighLowDataHandler;
	private       IHistoricalDataHandler adrDataHandler;
	private       List<Double>           adrList             = new java.util.ArrayList<>(ADR_BARS);
	private       boolean                requestedMarketData = false;

	public IbkrBroker(String ticker, String exchange, Trigger disconnectionListener) throws BrokerException {
		log.debug("init start");

		try {
			this.ibkrAdapter = IbkrAdapterFactory.getAdapter();
			this.ibkrAdapter.setDisconnectionListener(disconnectionListener);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage(), e);
		}

		initHandlers();

		initContract(ticker, exchange);

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
		List<ContractDetails> contractDetailsList = ibkrAdapter.lookupContract(contract);
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
				notifyTradeContext();
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
				notifyTradeContext();
			}
		};

		adrDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				adrList.add(bar.high() - bar.low());
			}

			@Override
			public void historicalDataEnd() {
				adrList = Collections.unmodifiableList(adrList);
				adr = adrList.stream().mapToDouble(Double::doubleValue).average().orElse(0);
				notifyTradeContext();
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

		if (!ibkrAdapter.isConnected()) throw new BrokerException("IBKR disconnected");

		ibkrAdapter.controller()
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
		if (!ibkrAdapter.isConnected()) throw new BrokerException("IBKR disconnected");
		ibkrAdapter.controller().reqTopMktData(contractDetails.contract(), "", false, false, mktDataHandler);
		ibkrAdapter.controller()
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
		ibkrAdapter.controller().cancelTopMktData(mktDataHandler);
		ibkrAdapter.controller().cancelHistoricalData(dayHighLowDataHandler);
		requestedMarketData = false;
	}
}
