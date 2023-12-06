package com.corn.trade.ibkr;

import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Ticker;
import com.corn.trade.jpa.JpaRepo;
import com.corn.trade.util.functional.Trigger;
import com.ib.client.*;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.controller.Bar;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.showErrorDlg;

public class AutoUpdate {
	private final Ibkr                   ibkr;
	private final JFrame                 frame;
	private final Set<Consumer<Boolean>> activateListeners = new HashSet<>();
	private final List<Trigger>          triggers          = new ArrayList<>();
	private       Consumer<Boolean>      tickerUpdateSuccessListener;
	private       boolean                autoUpdate;
	private       String                 ticker;
	private       String                 exchange;
	private       ContractDetails        contractDetails;
	private       ITopMktDataHandler     mktDataHandler;

	private IHistoricalDataHandler historicalDataHandler;
	private Double                 bestPrice;

	private double askPrice = 0;

	private double bidPrice = 0;

	private double atr = 0;

	private double high = 0;

	private double low = 0;

	private boolean isLong = true;

	private final List<Exchange> exchanges;
	private final List<Ticker>   tickers;
	private final JpaRepo<Ticker, Long> tickerRepo;
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

	public void setAtr(double atr) {
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
			showErrorDlg(frame, "No contract details found for " + ticker);
			return false;
		} else if (contractDetailsList.size() > 1) {
			showErrorDlg(frame, "Multiple contract details found for " + ticker);
			return false;
		} else if (!contractDetailsList.get(0).contract().primaryExch().equals(exchange)) {
			Exchange exchangeFound =
					exchanges.stream()
					         .filter(e -> e.getName().equals(contractDetailsList.get(0).contract().primaryExch()))
					         .findFirst()
					         .orElse(null);
			if (exchangeFound == null) {
				showErrorDlg(frame, "Probably wrong exchange for " + ticker);
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

	public void setAutoUpdate(boolean autoUpdate) {
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
			showErrorDlg(frame, "Ticker not set");
			announce();
			return false;
		}
		if (atr == 0) {
			showErrorDlg(frame, "ATR not set");
			announce();
			return false;
		}
		return true;
	}

	public void addActivateListener(Consumer<Boolean> listener) {
		activateListeners.add(listener);
	}

	public void addUpdater(Trigger trigger) {
		triggers.add(trigger);
	}

	private void announce() {
		triggers.forEach(Trigger::trigger);
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

}
