package com.corn.trade.broker;

import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.model.*;
import com.corn.trade.risk.RiskManager;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.OrderService;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.ActionType;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;
import com.corn.trade.type.TradeStatus;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Trigger;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.round;

public abstract class Broker {
	public static final    int                                  BARS_FOR_ADR      = 10;
	protected static final int                                  ADR_BARS         = 20;
	protected static final Logger                               log              = LoggerFactory.getLogger(Broker.class);
	protected final        Map<Integer, Consumer<TradeContext>> contextListeners = new HashMap<>();
	protected              List<Bar>                            adrBarList        = new java.util.ArrayList<>();
	protected              String                               exchangeName;
	protected              Double                               adr;
	protected              Double                               ask;
	protected              Double                               bid;
	protected              Double                               price;
	protected              Double                               dayHigh;
	protected              Double                               dayLow;
	protected              int                                  contextListenerId = 0;
	protected              String                               assetName;
	private                String                               name;
	private                boolean                              openPosition      = false;
	protected              OrderBracketIds                      bracketIds;
	private                ExchangeTime                         exchangeTime;
	private                int                                  positionListenerId = 0;

	public Broker(Trigger disconnectionTrigger) throws BrokerException {
		initConnection(disconnectionTrigger);
	}

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

	protected abstract void initConnection(Trigger disconnectionTrigger) throws BrokerException;

	public ExchangeTime getExchangeTime() throws BrokerException {
		if (exchangeName == null) {
			throw new IllegalStateException("Exchange name not set.");
		}
		if (exchangeTime == null) {
			Exchange exchange;
			try {
				exchange = new AssetService().getExchange(exchangeName);
			} catch (DBException e) {
				throw new BrokerException("Error getting exchange", e);
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

	public abstract int addPnListener(Consumer<PnL> pnlListener) throws BrokerException;

	public abstract int addPositionListener(Consumer<Position> positionListener) throws BrokerException;

	public abstract void removePositionListener(int id) throws BrokerException;

	protected abstract void requestPositionUpdates() throws BrokerException;

	public abstract void requestPnLUpdates() throws BrokerException;

	protected abstract void requestAdr() throws BrokerException;

	protected abstract void requestMarketData() throws BrokerException;

	protected abstract void cancelMarketData();

	public abstract void requestExecutionData(CompletableFuture<List<ExecutionData>> executions) throws BrokerException;

	public void openPosition(TradeData tradeData, RiskManager riskManager) throws BrokerException {
	    log.debug("Opening position for asset {} on exchange {}", assetName, exchangeName);
		TradeService tradeService = new TradeService();
		try {
			if (tradeService.getOpenTrade(assetName, exchangeName) != null) {
				throw new BrokerException("There is already an open position for this asset.");
			}
		} catch (DBException e) {
			throw new BrokerException(e);
		}

		// Place orders
		OrderType orderType = tradeData.getOrderStop() == null ? OrderType.LMT : OrderType.STP_LMT;
		bracketIds = placeOrderWithBracket(tradeData.getQuantity(),
		                                   tradeData.getOrderStop(),
		                                   tradeData.getOrderLimit(),
		                                   tradeData.getTechStopLoss() ==
		                                   null ? tradeData.getStopLoss() : tradeData.getTechStopLoss(),
		                                   tradeData.getTakeProfit(),
		                                   tradeData.getPositionType(),
		                                   orderType,
		                                   (mainExecution) -> {
			                                   // We must check the main order status to be sure that the position is opened
			                                   // And after that we can request for the new position updates
			                                   if (mainExecution) {
				                                   try {
					                                   Trade trade = tradeService.saveNewTrade(assetName, exchangeName, tradeData);
					                                   requestPositionUpdates();
					                                   positionListenerId = addPositionListener(position -> {
						                                   if (position.getQuantity() == 0) {
															   log.info("Position closed, removing positionListenerId = {}", positionListenerId);
															   removePositionListener(positionListenerId);
							                                   closePosition(trade.getId(), riskManager);
						                                   }
					                                   });
													   openPosition = true;
													   log.info("Position opened, positionListenerId = {}", positionListenerId);
				                                   } catch (DBException e) {
					                                   log.error("Error saving new trade {}", e.getMessage());
				                                   } catch (BrokerException e) {
					                                   log.error("Error requesting position updates {}", e.getMessage());
				                                   }
			                                   }
		                                   });
	}

	protected void closePosition(long tradeId, RiskManager riskManager) {
		openPosition = false;
		TradeService tradeService = new TradeService();
		OrderService orderService = new OrderService();
		try {
			Trade trade = tradeService.updateTradeStatus(tradeId, TradeStatus.CLOSED);
			riskManager.countTrades();
			CompletableFuture<List<ExecutionData>> executions = new CompletableFuture<>();
			requestExecutionData(executions);
			executions.thenAcceptAsync(executionDataList -> executionDataList.forEach(executionData -> {
				if (executionData.time().plusSeconds(30).isAfter(trade.getCreatedAt())) {
					orderService.saveExecution(trade, bracketIds, executionData);
				}
			}));
		} catch (DBException e) {
			log.error("Error updating trade status {}", e.getMessage());
		}
	}

	public abstract OrderBracketIds placeOrderWithBracket(long qtt,
	                                                      Double stop,
	                                                      Double limit,
	                                                      Double stopLoss,
	                                                      Double takeProfit,
	                                                      PositionType positionType,
	                                                      OrderType orderType,
	                                                      Consumer<Boolean> mainExecutionListener) throws BrokerException;

	public abstract void placeOrder(long quantity,
	                                Double stop,
	                                Double limit,
	                                ActionType actionType,
	                                OrderType orderType,
	                                Consumer<Boolean> executionListener) throws BrokerException;


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

	public boolean isOpenPosition() {
		return openPosition;
	}

	public abstract void cleanAllOrders();

	public abstract void modifyStopLoss(long quantity, double stopLossPrice, ActionType actionType);
}
