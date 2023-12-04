package com.corn.trade.ibkr;

import com.corn.trade.util.Util;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.showErrorDlg;

public class AutoUpdate {
	private final Ibkr ibkr;
	private final JFrame                 frame;
	private final Set<Consumer<Boolean>> activateListeners = new HashSet<>();

	private Consumer<Boolean> tickerUpdateSuccessListener;
	private boolean           autoUpdate;
	private String  ticker;
	private String  exchange = "NYSE";
	private ContractDetails contractDetails;

	public AutoUpdate(JFrame frame, Ibkr ibkr) {
		this.ibkr = ibkr;
		this.frame = frame;
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
		contract.primaryExch(exchange);
		contract.exchange("SMART");
		contract.currency("USD");
		contract.secType("STK");
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
		this.autoUpdate = autoUpdate;
		activateListeners.forEach(listener -> listener.accept(autoUpdate));
	}

	public void addActivateListener(Consumer<Boolean> listener) {
		activateListeners.add(listener);
	}
}
