package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.entity.Exchange;
import com.ib.client.*;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Bar;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.showErrorDlg;

public class IbkrBroker implements Broker {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IbkrBroker.class);
	private final IbkrAdapter ibkrAdapter;
	private final Set<Consumer<Boolean>> activateListeners = new HashSet<>();
	private       boolean                autoUpdate;
	private       String                 ticker;
	private       String                 exchange;
	private ContractDetails    contractDetails;
	private ITopMktDataHandler mktDataHandler;

	private IHistoricalDataHandler historicalDataHandler;
	private Double                 bestPrice;

	private double askPrice = 0;

	private double bidPrice = 0;

	private Double atr = null;

	private double high = 0;

	private double low = 0;

	private boolean isLong = true;
	private Timer simulationTimer;
	private boolean directionUp = true;

	public IbkrBroker(String ticker, String exchange) throws BrokerException {
		log.debug("init start");
		this.ibkrAdapter = new IbkrAdapter();
		ibkrAdapter.run();
		log.debug("testing connection");
		if (!ibkrAdapter.isConnected()) {
			throw new BrokerException("Not connected to IBKR.");
		}
		log.debug("connected");

		initHandlers();

		Contract contract = new Contract();
		contract.symbol(ticker);
		contract.secType("STK");
		contract.primaryExch(exchange);

		if (exchange.equals("NYSE") || exchange.equals("NASDAQ")) {
			contract.exchange("SMART");
			contract.currency("USD");
		} else
			contract.exchange(exchange);

		log.debug("looking up contract");
		List<ContractDetails> contractDetailsList = ibkrAdapter.lookupContract(contract);
		if (contractDetailsList.isEmpty()) {
			throw new BrokerException("No contract details found for " + ticker);
		} else if (contractDetailsList.size() > 1) {
			throw new BrokerException("Multiple contract details found for " + ticker);
		}
		log.debug("contract details found");

		this.exchange = contractDetailsList.get(0).contract().primaryExch();
		contractDetails = contractDetailsList.get(0);
		log.debug("init finish");
	}

	public void setLong(boolean aLong) {
		isLong = aLong;
		bestPrice = aLong ? askPrice : bidPrice;
	}

	public void setAtr(Double atr) {
		this.atr = atr;
	}

	public boolean isReady() {
		return ibkrAdapter.isConnected();
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void startSimulation(double initialAskPrice, double initialBidPrice, double highValue, double lowValue, double step) {

		if (this.askPrice == 0 && this.bidPrice == 0) {
			this.askPrice = initialAskPrice;
			this.bidPrice = initialBidPrice;
		}

		if (simulationTimer != null && simulationTimer.isRunning()) {
			simulationTimer.stop(); // Stop any existing simulation
		}

		simulationTimer = new Timer(1000, e -> {
			// Update the prices
			if (directionUp) {
				askPrice = Math.min(askPrice + step, highValue);
				bidPrice = Math.min(bidPrice + step, highValue);
				if (askPrice == highValue || bidPrice == highValue) {
					directionUp = false;
				}
			} else {
				askPrice = Math.max(askPrice - step, lowValue);
				bidPrice = Math.max(bidPrice - step, lowValue);
				if (askPrice == lowValue || bidPrice == lowValue) {
					directionUp = true;
				}
			}

			if (isLong)
				bestPrice = askPrice;
			else
				bestPrice = bidPrice;
		});

		simulationTimer.start();
	}

	public void stopSimulation() {
		if (simulationTimer != null && simulationTimer.isRunning()) {
			simulationTimer.stop();
		}
	}

	public void setAutoUpdate(boolean autoUpdate) {
		if (contractDetails == null) {
			if (atr == null) {
				showErrorDlg(null, "ATR not set", true);
				return;
			}
			this.high = 34.01;
			this.low = 32.99;
			this.autoUpdate = autoUpdate;
			activateListeners.forEach(listener -> listener.accept(autoUpdate));
			if (autoUpdate)
				startSimulation(33.0, 33.03, 34.0, 33.0, 0.01);
			else
				stopSimulation();
			return;
		}
		if (!validate(autoUpdate)) {
			return;
		}
		this.autoUpdate = autoUpdate;
		activateListeners.forEach(listener -> listener.accept(autoUpdate));

		if (autoUpdate) {
			ibkrAdapter.controller().reqTopMktData(contractDetails.contract(),
			                                       "",
			                                       false,
			                                       false,
			                                       mktDataHandler
			);
			ibkrAdapter.controller().reqHistoricalData(contractDetails.contract(),
			                                           "",
			                                           1,
			                                           Types.DurationUnit.DAY,
			                                           Types.BarSize._1_day,
			                                           Types.WhatToShow.TRADES,
			                                           true,
			                                           true,
			                                           historicalDataHandler);
		} else {
			ibkrAdapter.controller().cancelTopMktData(mktDataHandler);
			ibkrAdapter.controller().cancelHistoricalData(historicalDataHandler);
		}
	}

	private boolean validate(boolean autoUpdate) {
		if (!autoUpdate) {
			return true;
		}
		if (contractDetails == null) {
			showErrorDlg(null, "Ticker not set", true);
			return false;
		}
		if (atr == null) {
			showErrorDlg(null, "ATR not set", true);
			return false;
		}
		return true;
	}

	public void addActivateListener(Consumer<Boolean> listener) {
		activateListeners.add(listener);
	}

	public Double getBestPrice() {
		return bestPrice;
	}

	public double getSpread() {
		return Math.abs(askPrice - bidPrice);
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	private void initHandlers() {
		mktDataHandler = new ApiController.TopMktDataAdapter() {
			@Override
			public void tickPrice(TickType tickType, double price, TickAttrib attribs) {
				if (tickType == TickType.ASK) {
					if (isLong)
						bestPrice = price;
					askPrice = price;
				}
				if (tickType == TickType.BID) {
					if (!isLong)
						bestPrice = price;
					bidPrice = price;
				}
			}
		};
		historicalDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				high = bar.high();
				low = bar.low();
			}

			@Override
			public void historicalDataEnd() {
			}
		};
	}

	public ContractDetails getContractDetails() {
		return contractDetails;
	}

	@Override
	public String getExchangeName() {
		return exchange;
	}
}
