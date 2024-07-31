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
package com.corn.trade.ui.controller;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerFactory;
import com.corn.trade.model.Position;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.type.ActionType;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;
import com.corn.trade.ui.component.position.PositionRow;
import com.corn.trade.ui.view.PositionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Math.abs;

/**
 * Controller for position view. Supports many positions tracking at once.
 */
public class PositionController {
	private static final Logger                   log           = LoggerFactory.getLogger(PositionController.class);
	private final        PositionView             view;
	private final        Map<String, PositionRow> positionRows;
	private final        Map<String, Position>    positions;
	private final        Map<String, Long>        oldQuantities = new HashMap<>();
	private final        Map<String, Boolean>     locked        = new HashMap<>();
	private final        Map<String, Boolean>     soldBE        = new HashMap<>();

	public PositionController(PositionView view) {
		this.view = view;
		this.positionRows = new HashMap<>();
		this.positions = new HashMap<>();
	}

	private static void lockAllButtons(PositionRow positionRow) {
		positionRow.getButton100().setEnabled(false);
		positionRow.getButton75().setEnabled(false);
		positionRow.getButton50().setEnabled(false);
	}

	private static void lockSellBeButton(PositionRow positionRow) {
		if (!positionRow.getButtonSellBE().isEnabled())
			return;
		log.info("Locking sell BE button");
		positionRow.getButtonSellBE().setEnabled(false);
	}

	private void unlockSellBeButton(PositionRow positionRow, String symbol) {
		if (soldBE.getOrDefault(symbol, false) )
			return;
		if (positionRow.getButtonSellBE().isEnabled())
			return;
		boolean sold = soldBE.getOrDefault(symbol, false);
		boolean enabled = positionRow.getButtonSellBE().isEnabled();
		log.info("Unlocking sell BE button, sold {}, enabled {}", sold, enabled);
		positionRow.getButtonSellBE().setEnabled(true);
	}

	private static boolean isBefore(PositionType positionType, double price, double aPoint) {
		return (positionType == PositionType.LONG && price < aPoint) ||
		       (positionType == PositionType.SHORT && price > aPoint);
	}

	private static double getUnrealizedPnl(Position position, long qtt, double price) {
		double unrealizedPnl = position.getUnrealizedPnl();

		if (unrealizedPnl == Double.MAX_VALUE) unrealizedPnl = 0.0; // IBKR API sends Double.MAX_VALUE as null value
		else {
			// subtract commission for closing position from
			// unrealized profit to get more accurate data
			// actually this code calculates commission only for IBKR/USD currently, because I don't use
			// other brokers in my trading at the moment. It should be extended to support other brokers and currencies
			unrealizedPnl -= TradeCalc.estimatedCommissionIbkrUSD(qtt, price);
		}
		return unrealizedPnl;
	}

	/**
	 * Updates position state in real time
	 *
	 * @param brokerName - broker name
	 * @param tradeData  - current trade context
	 * @param position   - current position data
	 */
	public void updatePosition(String brokerName, TradeData tradeData, Position position) {
		Broker broker = BrokerFactory.findBroker(brokerName)
		                             .orElseThrow(() -> new IllegalArgumentException("Broker not found - " +
		                                                                             brokerName +
		                                                                             "!"));
		String symbol = position.getSymbol();
		// Get or create UI view for the position ============================================
		PositionRow positionRow = positionRows.computeIfAbsent(symbol, view::addPosition);

		long         qtt          = abs(position.getQuantity()); // current quantity
		double       be           = tradeData.getBreakEven(); // break even price
		double       sl           = tradeData.getStopLoss(); // stop loss price
		PositionType positionType = tradeData.getPositionType(); // Long or Short position
		ActionType   action       = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;

		if (!positions.containsKey(symbol)) {
			// Initialize button listeners only once for the new position
			initButtonListeners(broker, tradeData, symbol, positionRow);
			positionRow.getButtonBE()
			           .addActionListener(e -> broker.modifyStopLoss(qtt, be, action));
			positionRow.getButtonSellBE()
			           .addActionListener(e -> sellToBreakEven(broker, symbol, tradeData));
		}

		// Prepare data =================================================

		Long oldQtt = oldQuantities.get(symbol); // old quantity from previous position state


		double price  = abs(position.getMarketValue() / position.getQuantity()); // current price of the position
		double target = tradeData.getTarget(); // target price

		// R/R coefficient
		double rr = abs(price - be) / abs(be - sl);
		if (isBefore(positionType, price, be))
			rr = -rr;

		double unrealizedPnl = getUnrealizedPnl(position, qtt, price);

		oldQuantities.put(symbol, qtt); // save current quantity for future comparison
		positions.put(symbol, position); // save current position data


		// Update UI view =================================================
		positionRow.setBe(be);
		positionRow.setQtt(qtt + "/" + tradeData.getQuantity());
		positionRow.setSl(sl);
		positionRow.setTarget(target);
		positionRow.setRR(rr);
		positionRow.setPl(unrealizedPnl);

		if (isBefore(positionType, price, be)) {
			positionRow.setBeLabel("S_BE");
			positionRow.setPlColor(Color.RED.darker());
			lockSellBeButton(positionRow);
		} else if (unrealizedPnl >= 0 && (positionType == PositionType.LONG && price < target) || (positionType == PositionType.SHORT && price > target)) {
			double beLoss = getBeLossPercent(symbol, tradeData);
			if (beLoss <= 33.33) {
				positionRow.setBeLabel("(" + String.format("%.2f", beLoss)+")");
				positionRow.setPlColor(Color.CYAN.darker());
				unlockSellBeButton(positionRow, symbol);
			} else {
				positionRow.setBeLabel("S_BE");
				positionRow.setPlColor(Color.GREEN.darker());
				lockSellBeButton(positionRow);
			}
		} else {
			positionRow.setBeLabel("S_BE");
			positionRow.setPlColor(Color.GREEN.darker());
			lockSellBeButton(positionRow);
		}

		// Process data =================================================
		if (qtt == 0) { // if position is closed
			closePosition(broker, symbol);
		} else if (oldQtt != null && qtt < oldQtt) { // adjust stop loss quantity
			broker.modifyStopLoss(qtt, sl, action);
			broker.modifyTakeProfit(qtt, target, action);
		}
	}

	private double getBeLossPercent(String symbol, TradeData tradeData) {
		Position position = positions.get(symbol);
		double price = abs(position.getMarketValue() / position.getQuantity());

		long qtt = TradeCalc.calculateSharesToBE(
				tradeData.getPrice(),
				abs(tradeData.getQuantity()),
				tradeData.getTechStopLoss() != null ? tradeData.getTechStopLoss() : tradeData.getStopLoss(),
				price);

		long initialQtt = tradeData.getQuantity();
		long remainingQtt = initialQtt - qtt;

		double currentProfit = abs(price - tradeData.getPrice()) * qtt;
		currentProfit = currentProfit - TradeCalc.estimatedCommissionIbkrUSD(qtt, price) - TradeCalc.getTax(currentProfit);

		double profitDelta = abs(tradeData.getTakeProfit() - tradeData.getPrice());

		double expectedProfit = profitDelta * initialQtt;
		expectedProfit = expectedProfit - TradeCalc.estimatedCommissionIbkrUSD(initialQtt, tradeData.getPrice()) - TradeCalc.getTax(expectedProfit);
		expectedProfit = expectedProfit - TradeCalc.estimatedCommissionIbkrUSD(initialQtt, tradeData.getTakeProfit());

		double remainingProfit = profitDelta * remainingQtt;
		remainingProfit = remainingProfit - TradeCalc.estimatedCommissionIbkrUSD(remainingQtt, tradeData.getTakeProfit()) - TradeCalc.getTax(remainingProfit);
		remainingProfit = remainingProfit - TradeCalc.estimatedCommissionIbkrUSD(initialQtt, tradeData.getPrice()) + currentProfit;

		return 100.0 - remainingProfit / expectedProfit * 100.0;
	}

	private void sellToBreakEven(Broker broker, String symbol, TradeData tradeData) {
		if (locked.getOrDefault(symbol, false)) {
			log.warn("Position {} is currently locked", symbol);
			return;
		}
		locked.put(symbol, true); // lock the position to prevent multiple closures at once
		lockAllButtons(positionRows.get(symbol));
		Position position = positions.get(symbol);

		double price = abs(position.getMarketValue() / position.getQuantity());
		PositionType positionType = tradeData.getPositionType();

		long qtt = TradeCalc.calculateSharesToBE(
				tradeData.getPrice(),
				abs(tradeData.getQuantity()),
				tradeData.getTechStopLoss() != null ? tradeData.getTechStopLoss() : tradeData.getStopLoss(),
				price);

		ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
		log.info("Selling to break even for {}", symbol);

		broker.placeOrder(qtt,
		                  null,
		                  price,
		                  action,
		                  OrderType.LMT,
		                  executed -> {
			                    lockSellBeButton(positionRows.get(symbol));
								soldBE.put(symbol, true);
								getOrderExecutionHandler(symbol, qtt, abs(position.getQuantity())).accept(executed);
		                  });
	}

	private void closePosition(Broker broker, String symbol) {
		view.removePosition(symbol);
		positionRows.remove(symbol);
		positions.remove(symbol);
		oldQuantities.remove(symbol);
		log.debug("Position {} is closed for {}", symbol, broker.getName());
		broker.cleanAllOrders(); // cancel all active orders remaining for this position
	}

	private void cleanButtonListeners(PositionRow positionRow) {
		if (positionRow.getButton100().getActionListeners().length != 0) {
			positionRow.getButton100().removeActionListener(positionRow.getButton100().getActionListeners()[0]);
		}
		if (positionRow.getButton75().getActionListeners().length != 0) {
			positionRow.getButton75().removeActionListener(positionRow.getButton75().getActionListeners()[0]);
		}
		if (positionRow.getButton50().getActionListeners().length != 0) {
			positionRow.getButton50().removeActionListener(positionRow.getButton50().getActionListeners()[0]);
		}
		if (positionRow.getButtonBE().getActionListeners().length != 0) {
			positionRow.getButtonBE().removeActionListener(positionRow.getButtonBE().getActionListeners()[0]);
		}
	}

	private void initButtonListeners(Broker broker, TradeData tradeData, String symbol, PositionRow positionRow) {
		cleanButtonListeners(positionRow);
		positionRow.getButton100()
		           .addActionListener(getActionListener(symbol,
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
		positionRow.getButton75()
		           .addActionListener(getActionListener(symbol,
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
		positionRow.getButton50()
		           .addActionListener(getActionListener(symbol,
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
	}

	// Update button states based on the current quantity
	private void updateButtonStates(PositionRow positionRow, long initialQuantity, long currentQuantity) {
		double percentLeft = (double) currentQuantity / initialQuantity * 100;

		// enable only the buttons that remain within the current quantity
		positionRow.getButton100().setEnabled(true); // always enable 100% button
		positionRow.getButton75().setEnabled(percentLeft >= 75);
		positionRow.getButton50().setEnabled(percentLeft >= 50);
	}

	// Get action listener for the button
	private ActionListener getActionListener(String symbol,
	                                         PositionRow positionRow,
	                                         Broker broker,
	                                         PositionType positionType,
	                                         long initialQtt) {
		return e -> {
			log.info("Closing position for {}", symbol);
			if (locked.getOrDefault(symbol, false)) {
				log.warn("Position {} is currently locked", symbol);
				return;
			}
			locked.put(symbol, true); // lock the position to prevent multiple closures at once
			lockAllButtons(positionRow);

			JButton source = (JButton) e.getSource();
			long qtt = calculateQuantityForClosure(source,
			                                       positionRow,
			                                       initialQtt,
			                                       abs(positions.get(symbol).getQuantity()));

			double price = abs(positions.get(symbol).getMarketValue() / positions.get(symbol).getQuantity());
			log.info("Calculated price for {} = {}", qtt, price);

			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			double     delta  = positionType == PositionType.LONG ? -0.1 : 0.1;
			log.info("Position for {} is {}, delta = {}, limit price = {}", symbol, positionType, delta, price + delta);

			broker.placeOrder(qtt,
			                  null,
			                  price + delta,
			                  action,
			                  OrderType.LMT,
			                  getOrderExecutionHandler(symbol, qtt, initialQtt));
		};
	}

	// Get order execution handler for the position
	private Consumer<com.corn.trade.type.OrderStatus> getOrderExecutionHandler(String symbol,
	                                                                           long orderQuantity,
	                                                                           long initialQuantity) {
		return executed -> {
			if (executed.equals(OrderStatus.FILLED) && positions.containsKey(symbol)) {
				Position position    = positions.get(symbol);
				long     newQuantity = abs(position.getQuantity()) - abs(orderQuantity);
				updateButtonStates(positionRows.get(symbol), initialQuantity, newQuantity);
			} else if (positions.containsKey(symbol)) {
				updateButtonStates(positionRows.get(symbol), initialQuantity,
				                   abs(positions.get(symbol).getQuantity()));
			}
			locked.remove(symbol);
		};
	}

	// Calculate quantity for closure based on the button clicked
	// It's either percentage of the initial quantity or the current quantity if this is the last closure
	private long calculateQuantityForClosure(JButton source, PositionRow positionRow, long initialQtt, long currentQtt) {
		log.debug("Calculating quantity for closure, initial quantity: {}, current quantity: {}", initialQtt, currentQtt);
		if (source == positionRow.getButton75() && currentQtt > initialQtt * 0.75) {
			return (long) (initialQtt * 0.75);
		}
		if (source == positionRow.getButton50() && currentQtt > initialQtt * 0.5) {
			return (long) (initialQtt * 0.5);
		}
		return currentQtt;
	}
}
