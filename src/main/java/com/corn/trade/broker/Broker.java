package com.corn.trade.broker;

import com.corn.trade.trade.analysis.data.TradeContext;

import java.util.function.Supplier;

public interface Broker {
	String getExchangeName();
	void requestTradeContext(Supplier<TradeContext> tradeContextSupplier);
	void cancelTradeContext();
}
