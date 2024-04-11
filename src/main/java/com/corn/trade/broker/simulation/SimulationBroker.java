package com.corn.trade.broker.simulation;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.model.Bar;
import com.corn.trade.model.ExecutionData;
import com.corn.trade.model.PnL;
import com.corn.trade.model.Position;
import com.corn.trade.type.ActionType;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;
import com.corn.trade.util.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SimulationBroker extends Broker {
	private static final long                             TRADE_CONTEXT_UPDATE_INTERVAL = 500;
	private static final long                             POSITION_UPDATE_INTERVAL      = 1000;
	private final        Logger                           log                           =
			LoggerFactory.getLogger(SimulationBroker.class);
	private final        Map<Integer, Consumer<PnL>>      pnlListeners                  = new HashMap<>();
	private final        Map<Integer, Consumer<Position>> positionListeners             = new ConcurrentHashMap<>();
	private final        java.util.Timer                            tradeContextTimer;

	private final Timer                 positionTimer;
	private final TradeContextGenerator tradeContextGenerator;
	private       Trigger               disconnectionTrigger;
	private       int                   pnlListenerId      = 0;
	private       int                   positionListenerId = 0;
	private       long                  quantity           = 100;
	private       Double                startPrice;
	private       PositionType          positionType;
	private       Double                unRealizedPnl;
	private       Double                realizedPnl        = 0.0;

	public SimulationBroker(Trigger disconnectionTrigger) throws BrokerException {
		super(disconnectionTrigger);
		exchangeName = "TEST";
		tradeContextGenerator = new TradeContextGenerator();
		tradeContextTimer = new java.util.Timer("tradeContextTimer", false);


		positionTimer = new Timer((int) POSITION_UPDATE_INTERVAL, e -> {
			if (quantity > 0) {
				unRealizedPnl = getUnrealizedPnl();
			}
			Position position = Position.aPosition()
			                            .withSymbol(assetName)
			                            .withQuantity(quantity)
			                            .withMarketValue(quantity * price)
			                            .withUnrealizedPnl(unRealizedPnl)
			                            .build();
			positionListeners.values().forEach(pl -> pl.accept(position));
		});
	}

	private Double getUnrealizedPnl() {
		if (positionType == PositionType.LONG) {
			return (price - startPrice) * quantity;
		} else if (positionType == PositionType.SHORT) {
			return (startPrice - price) * quantity;
		}
		return null;
	}

	@Override
	protected void initConnection(Trigger disconnectionTrigger) throws BrokerException {
		this.disconnectionTrigger = disconnectionTrigger;
	}

	@Override
	public synchronized int addPnListener(Consumer<PnL> pnlListener) throws BrokerException {
		pnlListeners.put(++pnlListenerId, pnlListener);
		return pnlListenerId;
	}

	@Override
	public synchronized int addPositionListener(Consumer<Position> positionListener) throws BrokerException {
		positionListeners.put(++positionListenerId, positionListener);
		return positionListenerId;
	}

	@Override
	public synchronized void removePositionListener(int id) throws BrokerException {
		log.debug("removePositionListener :Removing position listener with id {}", id);
		positionTimer.stop();
		positionListeners.remove(id);
		positionTimer.start();
	}

	@Override
	protected synchronized void requestPositionUpdates() throws BrokerException {
		positionTimer.start();
	}

	@Override
	public void requestPnLUpdates() throws BrokerException {
		log.debug("Requesting PnL updates");
	}

	@Override
	protected void requestAdr() throws BrokerException {
		log.debug("Requesting ADR");
		adrBarList = new ArrayList<>();
		adrBarList.add(Bar.aBar().withHigh(32.5).withLow(30).build());
		adrBarList.add(Bar.aBar().withHigh(32.5).withLow(30).build());
		adrBarList.add(Bar.aBar().withHigh(32.5).withLow(30).build());
		adrBarList.add(Bar.aBar().withHigh(32.5).withLow(30).build());
		adrBarList.add(Bar.aBar().withHigh(32.5).withLow(30).build());
	}

	@Override
	protected void requestMarketData() throws BrokerException {
		log.debug("Requesting market data");
		tradeContextTimer.scheduleAtFixedRate(new java.util.TimerTask() {
			@Override
			public void run() {
				TradeContextGenerator.Context context = tradeContextGenerator.next();
				ask = context.ask();
				bid = context.bid();
				price = context.price();
				dayHigh = context.high();
				dayLow = context.low();
				notifyTradeContext();
			}
		}, 0, TRADE_CONTEXT_UPDATE_INTERVAL);
	}

	@Override
	protected void cancelMarketData() {
		log.debug("Cancelling market data");
		tradeContextTimer.cancel();
	}

	@Override
	public void requestExecutionData(CompletableFuture<List<ExecutionData>> executions) throws BrokerException {
		// not needed for now
	}

	@Override
	public OrderBracketIds placeOrderWithBracket(long qtt,
	                                             Double stop,
	                                             Double limit,
	                                             Double stopLoss,
	                                             Double takeProfit,
	                                             PositionType positionType,
	                                             OrderType orderType,
	                                             Consumer<Boolean> mainExecutionListener) throws BrokerException {
		startPrice = price;
		quantity = qtt;
		this.positionType = positionType;
		mainExecutionListener.accept(true);
		return new OrderBracketIds("1", "2", "3");
	}

	@Override
	public void placeOrder(long quantity,
	                       Double stop,
	                       Double limit,
	                       ActionType actionType,
	                       OrderType orderType,
	                       Consumer<Boolean> executionListener) throws BrokerException {
		log.debug("Placing order with quantity " + quantity + " at price " + price);
		this.quantity -= quantity;
		executionListener.accept(true);
	}

	@Override
	public void cleanAllOrders() {
		log.debug("Cleaning all orders");
	}

	@Override
	public void setStopLossQuantity(long quantity, double stopLossPrice, ActionType actionType) {
		log.debug("Setting stop loss quantity to {} at price {}", quantity, stopLossPrice);
	}

	protected void closePosition(long tradeId) {
		super.closePosition(tradeId);
		realizedPnl += unRealizedPnl;
		log.debug("Closing position with realizedPnl {}, unRealizedPnl {}", realizedPnl, unRealizedPnl);
		log.debug("Pnl listeners size {} ", pnlListeners.size());
		pnlListeners.values().forEach(pl -> pl.accept(new PnL(realizedPnl, realizedPnl, unRealizedPnl)));
	}
}
