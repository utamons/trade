package com.corn.trade.broker;

import com.corn.trade.model.Bar;
import com.corn.trade.model.TradeContext;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.round;

public abstract class Broker {
	private static final Logger log = LoggerFactory.getLogger(Broker.class);

	public static final    int       BARS_FOR_ADR             = 10;
	protected static final int       ADR_BARS                 = 20;
	protected final HashMap<Integer, Consumer<TradeContext>> contextListeners = new HashMap<>();
	protected              List<Bar> adrBarList               = new java.util.ArrayList<>(ADR_BARS);
	protected              String    exchangeName;
	protected              Double    adr;
	protected              Double    ask;
	protected              Double    bid;
	protected              Double    price;
	protected              Double    dayHigh;
	protected              Double    dayLow;
	protected              int       contextListenerId        = 0;

	public String getExchangeName() {
		return exchangeName;
	}

	public synchronized int requestTradeContext(Consumer<TradeContext> tradeContextListener) throws BrokerException {
		if (contextListeners.isEmpty()) {
			requestAdr();
			requestMarketData();
		}
		contextListeners.put(++contextListenerId, tradeContextListener);
		return contextListenerId;
	}

	protected abstract void requestAdr() throws BrokerException;

	protected abstract void requestMarketData() throws BrokerException;

	protected abstract void cancelMarketData();

	protected void notifyTradeContext() {
		calculateFilteredAdr();
		if (contextListeners.isEmpty()) return;
		TradeContext context = createTradeContext();
		contextListeners.values().forEach(listener -> listener.accept(context));
	}

	private void calculateFilteredAdr() {
		if (adr != null) return;

		final double ADR_TOLERANCE_PERCENTAGE = 40.0;

		adrBarList.sort(Comparator.comparingLong(Bar::getTime));

		List<Double> rangeList = adrBarList.stream().map((b) -> (b.getHigh() - b.getLow())).toList();

		double initialAverage = rangeList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

		double toleranceValue           = initialAverage * (ADR_TOLERANCE_PERCENTAGE / 100);

		List<Double> filteredList =
				rangeList.stream().filter(value -> Math.abs(value - initialAverage) <= toleranceValue).toList();

		if (filteredList.isEmpty()) {
			adr = round(initialAverage);
		}

		adr = round(getLastSome(filteredList).stream().mapToDouble(Double::doubleValue).average().orElse(initialAverage));

		log.debug("Initial ADR: {}", round(initialAverage));
		log.debug("ADR: {}", adr);
		log.debug("ADR Range: {}", String.join(",", getLastSome(filteredList).stream().map(Util::round).map(Object::toString).toList()));
	}

	protected TradeContext createTradeContext() {
		return TradeContext.TradeContextBuilder.aTradeContext()
		                                       .withAsk(ask)
		                                       .withBid(bid)
		                                       .withPrice(price)
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

	public List<Double> getLastSome(List<Double> rangeList) {
		if (rangeList.size() > BARS_FOR_ADR) {
			return rangeList.subList(rangeList.size() - BARS_FOR_ADR, rangeList.size());
		} else {
			return rangeList;
		}
	}
}
