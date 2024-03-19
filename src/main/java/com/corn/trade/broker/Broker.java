package com.corn.trade.broker;

import com.corn.trade.trade.analysis.data.TradeContext;

import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Broker {

	protected String exchangeName;
	protected Double adr;
	protected Double ask;
	protected Double bid;
	protected Double dayHigh;
	protected Double dayLow;
	protected int    contextListenerId = 0;

	protected HashMap<Integer, Consumer<TradeContext>> contextListeners = new HashMap<>();

	public String getExchangeName() {
		return exchangeName;
	}

	public synchronized int requestTradeContext(Consumer<TradeContext> tradeContextListener) {
		contextListenerId++;
		if (contextListeners.isEmpty()) {
			requestAdr();
			requestMarketData();
		}
		contextListeners.put(contextListenerId, tradeContextListener);
		return contextListenerId;
	}
	protected abstract void requestAdr();

	protected abstract void requestMarketData();

	protected abstract void cancelMarketData();

	protected void notifyTradeContext() {
		if (contextListeners.isEmpty()) return;
		TradeContext context = createTradeContext();
		contextListeners.values().forEach(listener -> listener.accept(context));
	}

	protected TradeContext createTradeContext() {
		return TradeContext.TradeContextBuilder.aTradeContext()
			.withAsk(ask)
			.withBid(bid)
			.withDayHigh(dayHigh)
			.withDayLow(dayLow)
			.withAdr(adr)
			.build();
	}

	public void cancelTradeContext(int id) {
		this.contextListeners.remove(id);
		if (this.contextListeners.isEmpty()) {
			cancelMarketData();
		}
	}
}
