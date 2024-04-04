package com.corn.trade.broker;

import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.model.*;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.round;

public abstract class Broker {
	public static final    int                                      BARS_FOR_ADR      = 10;
	protected static final int                                      ADR_BARS          = 20;
	private static final   Logger                                   log               =
			LoggerFactory.getLogger(Broker.class);
	protected final        HashMap<Integer, Consumer<TradeContext>> contextListeners  = new HashMap<>();
	protected              List<Bar>                                adrBarList        = new java.util.ArrayList<>();
	protected              String                                   exchangeName;
	protected              Double                                   adr;
	protected              Double                                   ask;
	protected              Double                                   bid;
	protected              Double                                   price;
	protected              Double                                   dayHigh;
	protected              Double                                   dayLow;
	protected              int                                      contextListenerId = 0;
	private                String                                   assetName;
	private                String                                   name;

	private OrderBracketIds bracketIds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAssetName() {
		return assetName;
	}

	protected void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	private ExchangeTime exchangeTime;

	public ExchangeTime getExchangeTime() throws BrokerException {
		if (exchangeName == null) {
			throw new IllegalStateException("Exchange name not set.");
		}
		if (exchangeTime == null) {
			Exchange exchange;
			try {
				exchange = new AssetService().getExchange(exchangeName);
			} catch (DBException e) {
				throw new BrokerException("Error getting exchange",e);
			}
			exchangeTime = new ExchangeTime(exchange);
		}
		return exchangeTime;
	}

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

	public abstract void requestExecutionData(CompletableFuture<List<ExecutionData>> executions) throws BrokerException;

	public void openPosition(TradeData tradeData) throws BrokerException {
		// Place orders
		OrderType orderType = tradeData.getOrderStop() == null ? OrderType.LMT : OrderType.STP_LMT;
		bracketIds = placeOrderWithBracket(tradeData.getQuantity(),
		                                                   tradeData.getOrderStop(),
		                                                   tradeData.getOrderLimit(),
		                                                   tradeData.getTechStopLoss() ==
		                                                   null ? tradeData.getStopLoss() : tradeData.getTechStopLoss(),
		                                                   tradeData.getTakeProfit(),
		                                                   tradeData.getPositionType(),
		                                                   orderType);
		TradeService tradeService = new TradeService();
		try {
			tradeService.saveNewTrade(assetName, exchangeName, tradeData);
		} catch (DBException e) {
			throw new BrokerException(e);
		}
		// We must check the main order status to be sure that the position is opened
		// And after that we can request for the new position updates
	}

	public abstract OrderBracketIds placeOrderWithBracket(long qtt,
	                                                      Double stop,
	                                                      Double limit,
	                                                      Double stopLoss,
	                                                      Double takeProfit,
	                                                      PositionType positionType,
	                                                      OrderType orderType) throws BrokerException;

	protected void notifyTradeContext() throws BrokerException {
		calculateFilteredAdr();
		if (contextListeners.isEmpty()) return;
		TradeContext context = createTradeContext();
		contextListeners.values().forEach(listener -> listener.accept(context));
	}

	private void calculateFilteredAdr() throws BrokerException {
		if (adr != null) return;
		if (adrBarList.isEmpty()) {
			throw new BrokerException("No ADR data available.");
		}

		final double ADR_TOLERANCE_PERCENTAGE = 40.0;

		adrBarList.sort(Comparator.comparingLong(Bar::getTime));

		List<Double> rangeList = adrBarList.stream().map((b) -> (b.getHigh() - b.getLow())).toList();

		double initialAverage = rangeList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

		double toleranceValue = initialAverage * (ADR_TOLERANCE_PERCENTAGE / 100);

		List<Double> filteredList =
				rangeList.stream().filter(value -> Math.abs(value - initialAverage) <= toleranceValue).toList();

		if (filteredList.isEmpty()) {
			adr = round(initialAverage);
		}

		adr = round(getLastSome(filteredList).stream().mapToDouble(Double::doubleValue).average().orElse(initialAverage));

		log.debug("Initial ADR: {}", round(initialAverage));
		log.debug("ADR: {}", adr);
		log.debug("ADR Range: {}",
		          String.join(",", getLastSome(filteredList).stream().map(Util::round).map(Object::toString).toList()));
	}

	protected TradeContext createTradeContext() {
		return TradeContext.TradeContextBuilder.aTradeContext()
		                                       .withAsk(ask)
		                                       .withBid(bid)
		                                       .withPrice(price)
		                                       .withDayHigh(dayHigh)
		                                       .withDayLow(dayLow)
		                                       .withAdr(adr)
		                                       .withPositionOpen(false)
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
