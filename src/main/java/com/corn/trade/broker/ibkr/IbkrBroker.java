package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.service.AssetService;
import com.corn.trade.util.Trigger;
import com.ib.client.*;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Bar;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IbkrBroker extends Broker {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IbkrBroker.class);

	private final IbkrAdapter            ibkrAdapter;
	private final ContractDetails        contractDetails;
	private       ITopMktDataHandler     mktDataHandler;
	private       IHistoricalDataHandler dayHighLowDataHandler;
	private       IHistoricalDataHandler adrDataHandler;
	private       List<Double>           adrList;

	public IbkrBroker(String ticker, String exchange, Trigger disconnectionTrigger) throws BrokerException {
		log.debug("init start");

		try {
			this.ibkrAdapter = IbkrAdapterFactory.getAdapter();
			this.ibkrAdapter.setDisconnectionTrigger(disconnectionTrigger);
		} catch (IbkrException e) {
			throw new BrokerException(e.getMessage());
		}

		initHandlers();

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
		log.debug("init finish");
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
				notifyTradeContext();
			}
		};
		dayHighLowDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				dayHigh = bar.high();
				dayLow = bar.low();
				notifyTradeContext();
			}

			@Override
			public void historicalDataEnd() {
			}
		};

		adrDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				adrList.add(bar.high() - bar.low());
			}

			@Override
			public void historicalDataEnd() {
				adr = adrList.stream().mapToDouble(Double::doubleValue).average().orElse(0);
				notifyTradeContext();
			}
		};
	}

	ContractDetails getContractDetails() {
		return contractDetails;
	}

	protected void requestAdr() {
		if (adr != null) {
			notifyTradeContext();
			return;
		}
		Exchange exchange;
		try {
			exchange = new AssetService().getExchange(exchangeName);
		} catch (DBException e) {
			throw new IbkrException(e);
		}

		String    timezone     = exchange.getTimeZone();
		String    tradingHours = exchange.getTradingHours();
		String[]  hoursParts   = tradingHours.split("-");
		LocalTime endTrading   = LocalTime.parse(hoursParts[1], DateTimeFormatter.ofPattern("HH:mm"));

		// Get current time in exchange's timezone
		ZonedDateTime nowInExchangeTimeZone = ZonedDateTime.now(ZoneId.of(timezone));

		// Determine if current time is after trading hours
		boolean isAfterTradingHours = nowInExchangeTimeZone.toLocalTime().isAfter(endTrading);

		// If the current time is after trading hours, use the current date as the last trading day
		// Otherwise, use the previous day as the last trading day
		ZonedDateTime lastTradingDayEnd = isAfterTradingHours ? nowInExchangeTimeZone : nowInExchangeTimeZone.minusDays(1);

		// Format lastDayEnd in YYYYMMDD HH:MM:SS format
		String lastDayEnd = lastTradingDayEnd.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
		                    " " +
		                    endTrading.format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
		                    " " +
		                    timezone;

		adrList = new java.util.ArrayList<>();
		ibkrAdapter.controller()
		           .reqHistoricalData(contractDetails.contract(),
		                              lastDayEnd,
		                              14,
		                              Types.DurationUnit.DAY,
		                              Types.BarSize._1_day,
		                              Types.WhatToShow.TRADES,
		                              true,
		                              false,
		                              adrDataHandler);
	}


	@Override
	protected void requestMarketData() {
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
	}

	@Override
	protected void cancelMarketData() {
		ibkrAdapter.controller().cancelTopMktData(mktDataHandler);
		ibkrAdapter.controller().cancelHistoricalData(dayHighLowDataHandler);
	}
}
