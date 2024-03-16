package com.corn.trade.broker.ibkr;

import com.corn.trade.common.Notifier;
import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Ticker;
import com.corn.trade.jpa.JpaRepo;
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

public class AutoUpdate extends Notifier {
	private final Ibkr                   ibkr;
	private final JFrame                 frame;
	private final Set<Consumer<Boolean>> activateListeners = new HashSet<>();
	private       Consumer<Boolean>      tickerUpdateSuccessListener;
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

	private final List<Exchange> exchanges;
	private final List<Ticker>   tickers;
	private final JpaRepo<Ticker, Long> tickerRepo;

	private Timer simulationTimer;
	private boolean directionUp = true;

	public AutoUpdate(JFrame frame, Ibkr ibkr) {
		this.ibkr = ibkr;
		this.frame = frame;
		initHandlers();
		JpaRepo<Exchange, Long> exchangeRepo = new JpaRepo<>(Exchange.class);
		tickerRepo   = new JpaRepo<>(Ticker.class);

		exchanges = exchangeRepo.findAll().stream().sorted().toList();
		tickers = tickerRepo.findAll().stream().sorted().toList();
		if (!exchanges.isEmpty())
			exchange = exchanges.get(0).getName();
	}

	public void setLong(boolean aLong) {
		isLong = aLong;
		bestPrice = aLong ? askPrice : bidPrice;
		announce();
	}

	public void setAtr(Double atr) {
		this.atr = atr;
	}

	public boolean isReady() {
		return ibkr.isConnected();
	}

	public void setTickerUpdateSuccessListener(Consumer<Boolean> tickerUpdateSuccessListener) {
		this.tickerUpdateSuccessListener = tickerUpdateSuccessListener;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public void setTicker(String ticker) {
		if (ticker == null || ticker.equals(this.ticker) || !isReady()) {
			return;
		}
		contractDetails = null;
		if (lookup(ticker)) {
			this.ticker = ticker;
			notifyTickerUpdate(true);
		} else {
			this.ticker = null;
			notifyTickerUpdate(false);
		}
	}

	public void notifyTickerUpdate(boolean success) {
		if (tickerUpdateSuccessListener != null)
			tickerUpdateSuccessListener.accept(success);
	}

	private boolean lookup(String ticker) {
		Contract contract = new Contract();
		contract.symbol(ticker);
		contract.secType("STK");
		contract.primaryExch(exchange);

		if (exchange.equals("NYSE") || exchange.equals("NASDAQ")) {
			contract.exchange("SMART");
			contract.currency("USD");
		} else
			contract.exchange(exchange);

		List<ContractDetails> contractDetailsList = ibkr.lookupContract(contract);
		if (contractDetailsList.isEmpty()) {
			showErrorDlg(frame, "No contract details found for " + ticker, true);
			return false;
		} else if (contractDetailsList.size() > 1) {
			showErrorDlg(frame, "Multiple contract details found for " + ticker, true);
			return false;
		} else if (!contractDetailsList.get(0).contract().primaryExch().equals(exchange)) {
			Exchange exchangeFound =
					exchanges.stream()
					         .filter(e -> e.getName().equals(contractDetailsList.get(0).contract().primaryExch()))
					         .findFirst()
					         .orElse(null);
			if (exchangeFound == null) {
				showErrorDlg(frame, "Probably wrong exchange for " + ticker, true);
				return false;
			} else {
				exchange = exchangeFound.getName();
				announce();
			}
		}
		contractDetails = contractDetailsList.get(0);

		if (tickers.stream().noneMatch(t -> t.getName().equals(ticker))) {
			Ticker tickerEntity = new Ticker();
			tickerEntity.setName(ticker);
			tickerEntity.setExchange(exchanges.stream().filter(e -> e.getName().equals(exchange)).findFirst().orElse(null));
			tickerRepo.save(tickerEntity);
		}

		return true;
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

			// Notify any listeners about the price update
			announce();
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
				showErrorDlg(frame, "ATR not set", true);
				announce();
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
			ibkr.controller().reqTopMktData(contractDetails.contract(),
			                                "",
			                                false,
			                                false,
			                                mktDataHandler
			);
			ibkr.controller().reqHistoricalData(contractDetails.contract(),
			                                    "",
			                                    1,
			                                    Types.DurationUnit.DAY,
			                                    Types.BarSize._1_day,
			                                    Types.WhatToShow.TRADES,
			                                    true,
			                                    true,
			                                    historicalDataHandler);
		} else {
			ibkr.controller().cancelTopMktData(mktDataHandler);
			ibkr.controller().cancelHistoricalData(historicalDataHandler);
		}
	}

	private boolean validate(boolean autoUpdate) {
		if (!autoUpdate) {
			return true;
		}
		if (contractDetails == null) {
			showErrorDlg(frame, "Ticker not set", true);
			announce();
			return false;
		}
		if (atr == null) {
			showErrorDlg(frame, "ATR not set", true);
			announce();
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
					announce();
				}
				if (tickType == TickType.BID) {
					if (!isLong)
						bestPrice = price;
					bidPrice = price;
					announce();
				}
			}
		};
		historicalDataHandler = new IHistoricalDataHandler() {
			@Override
			public void historicalData(Bar bar) {
				high = bar.high();
				low = bar.low();
				announce();
			}

			@Override
			public void historicalDataEnd() {
			}
		};
	}

	public ContractDetails getContractDetails() {
		return contractDetails;
	}
}
