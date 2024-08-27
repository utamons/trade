/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.broker.simulation;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.model.*;
import com.corn.trade.risk.RiskManager;
import com.corn.trade.type.ActionType;
import com.corn.trade.type.OrderStatus;
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
	private static final long                             TRADE_CONTEXT_UPDATE_INTERVAL = 1000;
	private static final long                             POSITION_UPDATE_INTERVAL      = 1000;
	private final        Logger                           log                           =
			LoggerFactory.getLogger(SimulationBroker.class);
	private final        Map<Integer, Consumer<PnL>>      pnlListeners                  = new HashMap<>();
	private final        Map<Integer, Consumer<Position>> positionListeners             = new ConcurrentHashMap<>();
	private final        Map<Integer, Consumer<AccountUpdate>> accountListeners = new ConcurrentHashMap<>();
	private final        java.util.Timer                  tradeContextTimer;

	private final Timer                 positionTimer;
	private final Timer                 accountTimer;
	private final TradeContextGenerator tradeContextGenerator;
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
		tradeContextGenerator = new TradeContextGenerator(false);
		tradeContextTimer = new java.util.Timer("tradeContextTimer", false);

		accountTimer = new Timer(1000, e -> accountListeners.forEach((key, value) -> value.accept(new AccountUpdate("Test", "AvailableFunds", "100000.00", null))));

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
			positionListeners.forEach((key, value) -> value.accept(position.copy().withListenerId(key).build()));
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
		Broker.log.debug("Initializing connection");
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
	public synchronized int addAccountListener(Consumer<AccountUpdate> accountListener) throws BrokerException {
		accountListeners.put(++positionListenerId, accountListener);
		return positionListenerId;
	}

	@Override
	public synchronized void removePositionListener(int id) throws BrokerException {
		log.debug("removePositionListener :Removing position listener with id {}", id);
		positionTimer.stop();
		if (positionListeners.remove(id) == null) {
			log.warn("removePositionListener :Position listener with id {} not found", id);
		} else {
			log.debug("removePositionListener :Position listener with id {} removed", id);
		}
		if (positionListeners.isEmpty()) {
			log.debug("removePositionListener :No more position listeners, stopping position updates");
			positionTimer.stop();
		} else {
			log.debug("removePositionListener :Position listeners remained {}:", positionListeners.size());
			for (Integer key : positionListeners.keySet()) {
				log.debug("removePositionListener : id {}", key);
			}
		}
	}

	@Override
	public void removeAllPositionListeners() throws BrokerException {
		log.debug("removeAllPositionListeners :Removing all position listeners");
		positionTimer.stop();
		positionListeners.clear();
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
	public void requestAccountUpdates() throws BrokerException {
		log.debug("Requesting account updates");
		accountTimer.start();
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
		executions.complete(new ArrayList<>());
	}

	@Override
	public OrderBracketIds placeOrderWithBracket(long qtt,
	                                             Double stop,
	                                             Double limit,
	                                             Double stopLoss,
	                                             Double takeProfit,
	                                             PositionType positionType,
	                                             OrderType orderType,
	                                             Consumer<com.corn.trade.type.OrderStatus> mainExecutionListener) throws BrokerException {
		startPrice = price;
		quantity = qtt;
		this.positionType = positionType;
		mainExecutionListener.accept(com.corn.trade.type.OrderStatus.FILLED);
		return new OrderBracketIds("1", "2", "3");
	}

	@Override
	public void placeOrder(long quantity,
	                       Double stop,
	                       Double limit,
	                       ActionType actionType,
	                       OrderType orderType,
	                       Consumer<com.corn.trade.type.OrderStatus> executionListener) throws BrokerException {
		log.debug("Placing order with quantity {} at price {} ", quantity, price);
		this.quantity -= quantity;
		executionListener.accept(OrderStatus.FILLED);
	}

	@Override
	public void cleanAllOrders() {
		log.debug("Cleaning all orders");
	}

	@Override
	public void modifyStopLoss(long quantity, double stopLossPrice, ActionType actionType) {
		log.debug("Modified stop loss to {} at price {}", quantity, stopLossPrice);
	}

	@Override
	public void modifyTakeProfit(long quantity, double takeProfitPrice, ActionType actionType) {
		log.debug("Modified take profit to {} at price {}", quantity, takeProfitPrice);
	}

	protected void closePosition(long tradeId, RiskManager riskManager) {
		super.closePosition(tradeId, riskManager);
		realizedPnl += unRealizedPnl;
		log.debug("Closing position with realizedPnl {}, unRealizedPnl {}", realizedPnl, unRealizedPnl);
		log.debug("Pnl listeners size {} ", pnlListeners.size());
		pnlListeners.values().forEach(pl -> pl.accept(new PnL(realizedPnl, realizedPnl, unRealizedPnl)));
	}
}
