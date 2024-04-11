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

	private static void unlockAllButtons(PositionRow positionRow) {
		if (positionRow == null) return;
		positionRow.getButton100().setEnabled(true);
		positionRow.getButton75().setEnabled(true);
		positionRow.getButton50().setEnabled(true);
		positionRow.getButton25().setEnabled(true);
	}

	private static boolean isBefore(PositionType positionType, double price, double be) {
		return (positionType == PositionType.LONG && price < be) || (positionType == PositionType.SHORT && price > be);
	}

	/**
	 * Returns a handler which triggers when order is executed
	 *
	 * @param symbol - a symbol of the order
	 * @return a handler for order execution
	 */
	private Consumer<Boolean> getOrderExecutionHandler(String symbol) {
		return (any) -> {
			locked.remove(symbol);
			PositionRow positionRow = positionRows.get(symbol);
			unlockAllButtons(positionRow);
		};
	}

	/**
	 * Updates position state in real time
	 *
	 * @param broker    - a broker object (provides broker's functionality)
	 * @param tradeData - current trade context
	 * @param position  - current position data
	 */
	public void updatePosition(Broker broker, TradeData tradeData, Position position) {
		PositionRow positionRow = positionRows.computeIfAbsent(position.getSymbol(), view::addPosition);
		if (!positions.containsKey(position.getSymbol())) {
			initButtonListeners(broker, tradeData, position, positionRow);
		}
		positions.put(position.getSymbol(), position);
		Long        oldQtt      = oldQuantities.get(position.getSymbol()); // old quantity from previous position state

		PositionType positionType = tradeData.getPositionType(); // Long or Short position
		long         initialQtt   = tradeData.getQuantity(); // initial quantity when position was opened
		long         qtt          = Math.abs(position.getQuantity()); // current quantity

		oldQuantities.put(position.getSymbol(), qtt); // save current quantity for future comparison

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
			view.removePosition(position.getSymbol());
			positionRows.remove(position.getSymbol());
			broker.cleanAllOrders(); // cancel all active orders remaining for this position
			oldQuantities.remove(position.getSymbol());
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

		// buttons setup ======================================================================================

	}

	private void initButtonListeners(Broker broker, TradeData tradeData, Position position, PositionRow positionRow) {
		positionRow.getButton100().addActionListener(getActionListener(position.getSymbol(), positionRow, broker,
		                                                               tradeData.getPositionType(), tradeData.getQuantity()));
		positionRow.getButton75().addActionListener(getActionListener(position.getSymbol(), positionRow, broker,
		                                                              tradeData.getPositionType(), tradeData.getQuantity()));
		positionRow.getButton50().addActionListener(getActionListener(position.getSymbol(), positionRow, broker,
		                                                              tradeData.getPositionType(), tradeData.getQuantity()));
		positionRow.getButton25().addActionListener(getActionListener(position.getSymbol(), positionRow, broker,
		                                                              tradeData.getPositionType(), tradeData.getQuantity()));
	}

	/**
	 * Returns an action listener for partially closing position
	 *
	 * @param symbol     - a symbol of the position
	 * @param positionRow  - current position row in the view
	 * @param broker       - a broker object (provides broker's functionality)
	 * @param positionType - Long or Short position
	 * @return an action listener for partially closing position
	 */

	private ActionListener getActionListener(String symbol,
	                                         PositionRow positionRow,
	                                         Broker broker,
	                                         PositionType positionType,
	                                         long initialQtt) {
		return e -> {
			Position position = positions.get(symbol);
			if (Boolean.TRUE.equals(locked.get(position.getSymbol()))) {
				return;
			}
			locked.put(position.getSymbol(), true);
			lockAllButtons(positionRow);

			JButton source = (JButton) e.getSource();
			long    qtt    = calculateQuantity(source, positionRow, initialQtt, Math.abs(position.getQuantity()));

			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;

			// todo Не уверен, что это правильный расчёт цены. Она может уже уйти от этой точки на тик или два.
			//  Возможно, надо получить текущую цену от брокера...
			//  Также надо доработать блокировку/разблокировку кнопок и снова всё протестить...
			double price = position.getMarketValue() / Math.abs(position.getQuantity());

			broker.placeOrder(qtt, null, price, action, OrderType.LMT, getOrderExecutionHandler(position.getSymbol()));
		};
	}

	private long calculateQuantity(JButton source, PositionRow positionRow, long initialQtt, long currentQtt) {
		if (source == positionRow.getButton75() && currentQtt > initialQtt * 0.75) return (long) (initialQtt * 0.75);
		if (source == positionRow.getButton50() && currentQtt > initialQtt * 0.5) return (long) (initialQtt * 0.5);
		if (source == positionRow.getButton25() && currentQtt > initialQtt * 0.25) return (long) (initialQtt * 0.25);
		return currentQtt;
	}
}
