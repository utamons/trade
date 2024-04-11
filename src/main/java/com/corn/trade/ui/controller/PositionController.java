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
	private static final Logger log = LoggerFactory.getLogger(PositionController.class);
	private final PositionView view;
	private final Map<String, PositionRow> positions;
	private final Map<String, Long>        oldQuantities = new HashMap<>();
	private final Map<String, Boolean>     locked        = new HashMap<>();

	public PositionController(PositionView view) {
		this.view = view;
		this.positions = new HashMap<>();
	}

	private static void cleanListeners(JButton button) {
		for (ActionListener al : button.getActionListeners()) {
			button.removeActionListener(al);
		}
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

	/**
	 * Returns a handler which triggers when order is executed
	 *
	 * @param symbol - a symbol of the order
	 * @return a handler for order execution
	 */
	private Consumer<Boolean> getOrderExecutionHandler(String symbol) {
		return (any) -> {
			locked.remove(symbol);
			PositionRow positionRow = positions.get(symbol);
			unlockAllButtons(positionRow);
		};
	}

	/**
	 * Updates position state in real time
	 *
	 * @param broker - a broker object (provides broker's functionality)
	 * @param tradeData - current trade context
	 * @param position - current position data
	 */
	public void updatePosition(Broker broker, TradeData tradeData, Position position) {
		PositionRow positionRow = positions.computeIfAbsent(position.getSymbol(), view::addPosition);
		Long		oldQtt      = oldQuantities.get(position.getSymbol()); // old quantity from previous position state

        // buttons for partially closing position (100%, 75%, 50%, 25%)
		JButton btn100 = positionRow.getButton100();
		JButton btn75  = positionRow.getButton75();
		JButton btn50  = positionRow.getButton50();
		JButton btn25  = positionRow.getButton25();

		PositionType positionType = tradeData.getPositionType(); // Long or Short position
		long         initialQtt   = tradeData.getQuantity(); // initial quantity when position was opened
		long         qtt          = Math.abs(position.getQuantity()); // current quantity

		oldQuantities.put(position.getSymbol(), qtt); // save current quantity for future comparison
		double       percentLeft  = (double) qtt / initialQtt * 100; // percentage of quantity left compared to initial quantity

		double price = position.getMarketValue() / position.getQuantity(); // current price of the position
		double goal  = tradeData.getGoal(); // goal price
		double be    = tradeData.getBreakEven(); // break even price
		double sl    = tradeData.getStopLoss(); // stop loss price
		// distance from stop loss to goal
		double dst           = Math.abs(price - sl) / Math.abs(goal - sl) * 100;
		double unrealizedPnl = position.getUnrealizedPnl();

		if (unrealizedPnl == Double.MAX_VALUE) unrealizedPnl = 0.0; // IBKR API sends Double.MAX_VALUE as null value
		else {
			unrealizedPnl -= TradeCalc.estimatedCommissionIbkrUSD(qtt, price); // subtract commission for closing position from unrealized profit
		}

		if (qtt == 0) { // if position is closed
			view.removePosition(position.getSymbol());
			positions.remove(position.getSymbol());
			broker.cleanAllOrders(); // cancel all active orders remaining for this position
			oldQuantities.remove(position.getSymbol());
			return;
		} else if (oldQtt != null && qtt < oldQtt) { // set stop loss to new quantity
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			broker.setStopLossQuantity(qtt,sl, action);
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
		long qtt75 = (long) (initialQtt * 0.75);
		long qtt50 = (long) (initialQtt * 0.50);
		long qtt25 = (long) (initialQtt * 0.25);

		Consumer<Boolean> handler = getOrderExecutionHandler(position.getSymbol());

		if (locked.get(position.getSymbol()) != null) { // if partially closing buttons are locked (order is being executed)
			//noinspection UnnecessaryReturnStatement
			return;
		} else if (percentLeft == 100) { // re-set listeners for buttons
			cleanListeners(btn100);
			cleanListeners(btn75);
			cleanListeners(btn50);
			cleanListeners(btn25);
			btn100.addActionListener(getActionListener(position,
			                                           positionRow,
			                                           broker,
			                                           position.getQuantity(),
			                                           price,
			                                           positionType,
			                                           handler));
			btn75.addActionListener(getActionListener(position, positionRow, broker, qtt75, price, positionType, handler));
			btn50.addActionListener(getActionListener(position, positionRow, broker, qtt50, price, positionType, handler));
			btn25.addActionListener(getActionListener(position, positionRow, broker, qtt25, price, positionType, handler));
		} else if (percentLeft < 100 && percentLeft >= 75) { // re-set listeners for buttons
			btn100.setEnabled(false);
			cleanListeners(btn75);
			cleanListeners(btn50);
			cleanListeners(btn25);
			btn75.addActionListener(getActionListener(position,
			                                          positionRow,
			                                          broker,
			                                          position.getQuantity(),
			                                          price,
			                                          positionType,
			                                          handler));
			btn50.addActionListener(getActionListener(position, positionRow, broker, qtt50, price, positionType, handler));
			btn25.addActionListener(getActionListener(position, positionRow, broker, qtt25, price, positionType, handler));
		} else if (percentLeft < 75 && percentLeft >= 50) { // re-set listeners for buttons
			btn100.setEnabled(false);
			btn75.setEnabled(false);
			cleanListeners(btn50);
			cleanListeners(btn25);
			btn50.addActionListener(getActionListener(position,
			                                          positionRow,
			                                          broker,
			                                          position.getQuantity(),
			                                          price,
			                                          positionType,
			                                          handler));
			btn25.addActionListener(getActionListener(position, positionRow, broker, qtt25, price, positionType, handler));
		} else if (percentLeft < 50 && percentLeft >= 25) { // re-set listeners for buttons
			btn100.setEnabled(false);
			btn75.setEnabled(false);
			btn50.setEnabled(false);
			cleanListeners(btn25);
			btn25.addActionListener(getActionListener(position,
			                                          positionRow,
			                                          broker,
			                                          position.getQuantity(),
			                                          price,
			                                          positionType,
			                                          handler));
		}
	}

	private static boolean isBefore(PositionType positionType, double price, double be) {
		return (positionType == PositionType.LONG && price < be) || (positionType == PositionType.SHORT && price > be);
	}

	/**
	 * Returns an action listener for partially closing position
	 *
	 * @param position - current position data
	 * @param positionRow - current position row in the view
	 * @param broker - a broker object (provides broker's functionality)
	 * @param qtt - quantity to close
	 * @param price - current price of the position
	 * @param positionType - Long or Short position
	 * @param handler - a handler for order execution confirmation
	 *
	 * @return an action listener for partially closing position
	 */

	private ActionListener getActionListener(Position position,
	                                         PositionRow positionRow,
	                                         Broker broker,
	                                         long qtt,
	                                         double price,
	                                         PositionType positionType,
	                                         Consumer<Boolean> handler) {
		return e -> {
			log.debug("Partially closing position {} with quantity {}", position.getSymbol(), Math.abs(qtt));
			locked.put(position.getSymbol(), true);
			lockAllButtons(positionRow);
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			broker.placeOrder(Math.abs(qtt), null, price, action, OrderType.LMT, handler);
		};
	}
}
