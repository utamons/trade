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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("DuplicatedCode")
public class PositionController {
	private final PositionView             view;
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

	private Consumer<Boolean> getHandler(String symbol) {
		return (any) -> {
			locked.remove(symbol);
			PositionRow positionRow = positions.get(symbol);
			unlockAllButtons(positionRow);
		};
	}

	public void updatePosition(Broker broker, TradeData tradeData, Position position) {
		PositionRow positionRow = positions.computeIfAbsent(position.getSymbol(), view::addPosition);
		Long		oldQtt      = oldQuantities.get(position.getSymbol());


		JButton btn100 = positionRow.getButton100();
		JButton btn75  = positionRow.getButton75();
		JButton btn50  = positionRow.getButton50();
		JButton btn25  = positionRow.getButton25();

		PositionType positionType = tradeData.getPositionType();
		long         initialQtt   = tradeData.getQuantity();
		long         qtt          = position.getQuantity();
		oldQuantities.put(position.getSymbol(), qtt);
		double       percentLeft  = (double) qtt / initialQtt * 100;

		double price = position.getMarketValue() / position.getQuantity();
		double goal  = tradeData.getGoal();
		double be    = tradeData.getBreakEven();
		double sl    = tradeData.getStopLoss();
		// distance from stop loss to goal
		double dst           = Math.abs(price - sl) / Math.abs(goal - sl) * 100;
		double unrealizedPnl = position.getUnrealizedPnl();

		if (unrealizedPnl == Double.MAX_VALUE) unrealizedPnl = 0.0;
		else {
			unrealizedPnl -= TradeCalc.estimatedCommissionIbkrUSD(qtt, price);
		}

		/*
		  todo Стоит использовать для симуляции TSLA, она активно меняет контекст.
		   Нужно проверить, что с кнопками и обработчиками, если контекст активно меняется, при тестировании они не работали.
		   Когда всё оттестирую для LONG и буду в нём уверен, нужно потестировать для SHORT.
		 */
		if (qtt == 0) {
			view.removePosition(position.getSymbol());
			positions.remove(position.getSymbol());
			broker.cleanAllOrders();
			oldQuantities.remove(position.getSymbol());
			return;
		} else if (oldQtt != null && qtt < oldQtt) {
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			broker.setStopLossQuantity(qtt,sl, action);
		}

		positionRow.setQtt(qtt + "/" + tradeData.getQuantity());
		positionRow.setSl(sl);
		positionRow.setGoal(goal);
		positionRow.setDst(dst);
		positionRow.setPl(unrealizedPnl);

		if (unrealizedPnl >= 0 && price < be) {
			positionRow.setPlColor(Color.ORANGE.darker());
		} else if (unrealizedPnl >= 0) {
			positionRow.setPlColor(Color.GREEN.darker());
		} else {
			positionRow.setPlColor(Color.RED.darker());
		}

		if (price < be) {
			positionRow.setPsColor(Color.RED.darker());
		} else if (price < goal) {
			positionRow.setPsColor(Color.ORANGE.darker());
		} else {
			positionRow.setPsColor(Color.GREEN.darker());
		}

		long qtt75 = (long) (initialQtt * 0.75);
		long qtt50 = (long) (initialQtt * 0.50);
		long qtt25 = (long) (initialQtt * 0.25);

		Consumer<Boolean> handler = getHandler(position.getSymbol());

		if (locked.get(position.getSymbol()) != null) {
			//noinspection UnnecessaryReturnStatement
			return;
		} else if (percentLeft == 100) {
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
		} else if (percentLeft < 100 && percentLeft > 75) {
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
		} else if (percentLeft < 75 && percentLeft > 50) {
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
		} else if (percentLeft < 50 && percentLeft > 25) {
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

	private ActionListener getActionListener(Position position,
	                                         PositionRow positionRow,
	                                         Broker broker,
	                                         long qtt,
	                                         double price,
	                                         PositionType positionType,
	                                         Consumer<Boolean> handler) {
		return e -> {
			locked.put(position.getSymbol(), true);
			lockAllButtons(positionRow);
			ActionType action = positionType == PositionType.LONG ? ActionType.SELL : ActionType.BUY;
			broker.placeOrder(qtt, null, price, action, OrderType.LMT, handler);
		};
	}
}
