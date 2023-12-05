package com.corn.trade.ibkr;

import com.corn.trade.util.Util;
import com.corn.trade.util.functional.Trigger;
import com.ib.client.*;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.ITopMktDataHandler;

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
	private final List<Trigger> triggers = new ArrayList<>();
	private Consumer<Boolean> tickerUpdateSuccessListener;
	private boolean           autoUpdate;
	private String            ticker;
	private String            exchange = "NYSE";
	private ContractDetails   contractDetails;
	private ITopMktDataHandler mktDataHandler;
	private Double lastPrice;

	private double askPrice = 0;

	private double bidPrice = 0;

	private double atr = 0;

	public AutoUpdate(JFrame frame, Ibkr ibkr) {
		this.ibkr = ibkr;
		this.frame = frame;
		initHandlers();
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
		Util.log("Exchange set to " + exchange);
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
			showErrorDlg(frame, "Probably wrong exchange for " + ticker);
			return false;
		}
		contractDetails = contractDetailsList.get(0);
		Util.log("Contract details found for " + ticker);

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
		} else {
			ibkr.controller().cancelTopMktData(mktDataHandler);
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

	public Double getLastPrice() {
		return lastPrice;
	}

	public double getSpread() {
		return Math.abs(askPrice - bidPrice);
	}

	private void initHandlers() {
		mktDataHandler = new ApiController.TopMktDataAdapter() {
			@Override
			public void tickPrice(TickType tickType, double price, TickAttrib attribs) {
				if (tickType == TickType.LAST) {
					lastPrice = price;
					announce();
				}
				if (tickType == TickType.ASK) {
					askPrice = price;
					announce();
				}
				if (tickType == TickType.BID) {
					bidPrice = price;
					announce();
				}
				Util.log("tickPrice: " + tickType + " " + price);
			}

			@Override
			public void tickSize(TickType tickType, Decimal size) {
				Util.log("tickSize: " + tickType + " " + size);
			}

			@Override
			public void tickString(TickType tickType, String value) {
				Util.log("tickString: " + tickType + " " + value);
			}

			@Override
			public void tickSnapshotEnd() {
				Util.log("tickSnapshotEnd");
			}

			@Override
			public void marketDataType(int marketDataType) {
				Util.log("marketDataType: " + marketDataType);
			}

			@Override
			public void tickReqParams(int tickerId,
			                          double minTick,
			                          String bboExchange,
			                          int snapshotPermissions) {
				Util.log("tickReqParams: " + tickerId + " " + minTick + " " + bboExchange + " " + snapshotPermissions);
			}
		};
	}
}
