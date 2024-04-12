package com.corn.trade.ui.controller;

import com.corn.trade.broker.Broker;
import com.corn.trade.model.Position;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.type.ActionType;
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

/**
 * Controller for position view. Supports many positions tracking at once.
 */
@SuppressWarnings("DuplicatedCode")
public class PositionController {
	private static final Logger                   log           = LoggerFactory.getLogger(PositionController.class);
	private final        PositionView             view;
	private final        Map<String, PositionRow> positionRows;
	private final        Map<String, Position>    positions;
	private final        Map<String, Long>        oldQuantities = new HashMap<>();
	private final        Map<String, Boolean>     locked        = new HashMap<>();

	public PositionController(PositionView view) {
		this.view = view;
		this.positionRows = new HashMap<>();
		this.positions = new HashMap<>();
	}

	private static void lockAllButtons(PositionRow positionRow) {
		positionRow.getButton100().setEnabled(false);
		positionRow.getButton75().setEnabled(false);
		positionRow.getButton50().setEnabled(false);
		positionRow.getButton25().setEnabled(false);
	}

	private static boolean isBefore(PositionType positionType, double price, double be) {
		return (positionType == PositionType.LONG && price < be) || (positionType == PositionType.SHORT && price > be);
	}

	/**
	 * Updates position state in real time
	 *
	 * @param broker    - a broker object (provides broker's functionality)
	 * @param tradeData - current trade context
	 * @param position  - current position data
	 */
	public void updatePosition(Broker broker, TradeData tradeData, Position position) {
		String      symbol      = position.getSymbol();
		PositionRow positionRow = positionRows.computeIfAbsent(symbol, view::addPosition);
		if (!positions.containsKey(symbol)) {
			initButtonListeners(broker, tradeData, position, positionRow);
		}
		positions.put(symbol, position);
		Long oldQtt = oldQuantities.get(symbol); // old quantity from previous position state

		PositionType positionType = tradeData.getPositionType(); // Long or Short position
		long         qtt          = Math.abs(position.getQuantity()); // current quantity

		oldQuantities.put(symbol, qtt); // save current quantity for future comparison

		double price = position.getMarketValue() / position.getQuantity(); // current price of the position
		double goal  = tradeData.getGoal(); // goal price
		double be    = tradeData.getBreakEven(); // break even price
		double sl    = tradeData.getStopLoss(); // stop loss price
		// distance from stop loss to goal
		double dst           = Math.abs(price - sl) / Math.abs(goal - sl) * 100;
		double unrealizedPnl = position.getUnrealizedPnl();

		if (unrealizedPnl == Double.MAX_VALUE) unrealizedPnl = 0.0; // IBKR API sends Double.MAX_VALUE as null value
		else {
			unrealizedPnl -=
					TradeCalc.estimatedCommissionIbkrUSD(qtt, price); // subtract commission for closing position from
			// unrealized profit
		}

		if (qtt == 0) { // if position is closed
			closePosition(broker, symbol);
			return;
		} else if (oldQtt != null && qtt < oldQtt) { // set stop loss to new quantity
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			broker.setStopLossQuantity(qtt, sl, action);
		}

		// update position row in the view with new data
		positionRow.setQtt(qtt + "/" + tradeData.getQuantity());
		positionRow.setSl(sl);
		positionRow.setGoal(goal);
		positionRow.setDst(dst);
		positionRow.setPl(unrealizedPnl);

		if (unrealizedPnl >= 0 && isBefore(positionType, price, be)) {
			positionRow.setPlColor(Color.ORANGE.darker());
		} else if (unrealizedPnl >= 0) {
			positionRow.setPlColor(Color.GREEN.darker());
		} else {
			positionRow.setPlColor(Color.RED.darker());
		}

		if (isBefore(positionType, price, be)) {
			positionRow.setPsColor(Color.RED.darker());
		} else if (isBefore(positionType, price, goal)) {
			positionRow.setPsColor(Color.ORANGE.darker());
		} else {
			positionRow.setPsColor(Color.GREEN.darker());
		}
	}
	private void closePosition(Broker broker, String symbol) {
		view.removePosition(symbol);
		positionRows.remove(symbol);
		positions.remove(symbol);
		oldQuantities.remove(symbol);
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
		if (positionRow.getButton25().getActionListeners().length != 0) {
			positionRow.getButton25().removeActionListener(positionRow.getButton25().getActionListeners()[0]);
		}
	}

	private void initButtonListeners(Broker broker, TradeData tradeData, Position position, PositionRow positionRow) {
		cleanButtonListeners(positionRow);
		positionRow.getButton100()
		           .addActionListener(getActionListener(position.getSymbol(),
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
		positionRow.getButton75()
		           .addActionListener(getActionListener(position.getSymbol(),
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
		positionRow.getButton50()
		           .addActionListener(getActionListener(position.getSymbol(),
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
		positionRow.getButton25()
		           .addActionListener(getActionListener(position.getSymbol(),
		                                                positionRow,
		                                                broker,
		                                                tradeData.getPositionType(),
		                                                tradeData.getQuantity()));
	}

	private void updateButtonStates(PositionRow positionRow, long initialQuantity, long currentQuantity) {
		double percentLeft = (double) currentQuantity / initialQuantity * 100;

		positionRow.getButton100().setEnabled(percentLeft >= 100);
		positionRow.getButton75().setEnabled(percentLeft >= 75);
		positionRow.getButton50().setEnabled(percentLeft >= 50);
		positionRow.getButton25().setEnabled(percentLeft >= 25);
	}

	private ActionListener getActionListener(String symbol,
	                                         PositionRow positionRow,
	                                         Broker broker,
	                                         PositionType positionType,
	                                         long initialQtt) {
		return e -> {
			if (locked.getOrDefault(symbol, false)) {
				log.warn("Position {} is currently locked", symbol);
				return;
			}
			locked.put(symbol, true);
			lockAllButtons(positionRow);

			JButton source = (JButton) e.getSource();
			long    qtt    = calculateQuantityForClosure(source,
			                                             positionRow,
			                                             initialQtt,
			                                             Math.abs(positions.get(symbol).getQuantity()));

			double     price  = positions.get(symbol).getMarketValue() / Math.abs(positions.get(symbol).getQuantity());
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;

			broker.placeOrder(qtt, null, price, action, OrderType.LMT, getOrderExecutionHandler(symbol, qtt, initialQtt));
		};
	}

	private Consumer<Boolean> getOrderExecutionHandler(String symbol, long orderQuantity, long initialQuantity) {
		return executed -> {
			if (executed) {
				Position position = positions.get(symbol);
				if (position != null) {
					long newQuantity = position.getQuantity() - orderQuantity;
					updateButtonStates(positionRows.get(symbol), initialQuantity, newQuantity);
				}
			} else {
				updateButtonStates(positionRows.get(symbol), initialQuantity,
				                   Math.abs(positions.get(symbol).getQuantity()));
			}
			locked.remove(symbol);
		};
	}

	private long calculateQuantityForClosure(JButton source, PositionRow positionRow, long initialQtt, long currentQtt) {
		log.debug("Calculating quantity for closure, initial quantity: {}, current quantity: {}", initialQtt,
		          currentQtt);
		if (source == positionRow.getButton75() && currentQtt == initialQtt) {
			return (long) (initialQtt * 0.75);
		}
		if (source == positionRow.getButton50() && currentQtt >= initialQtt * 0.75) {
			return (long) (initialQtt * 0.5);
		}
		if (source == positionRow.getButton25() && currentQtt >= initialQtt * 0.5) {
			return (long) (initialQtt * 0.25);
		}
		return currentQtt;
	}
}
