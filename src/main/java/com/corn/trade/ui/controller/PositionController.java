package com.corn.trade.ui.controller;

import com.corn.trade.model.Position;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.ui.component.position.PositionRow;
import com.corn.trade.ui.view.PositionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PositionController {
	private final PositionView             view;
	private final Map<String, PositionRow> positions;

	public PositionController(PositionView view) {
		this.view = view;
		this.positions = new HashMap<>();
	}

	public void updatePosition(TradeData tradeData, Position position) {
		PositionRow positionRow = positions.computeIfAbsent(position.getSymbol(), view::addPosition);

		long qtt = position.getQuantity();

		if (qtt == 0) {
			view.removePosition(position.getSymbol());
			positions.remove(position.getSymbol());
			return;
		}

		double price = position.getMarketValue() / position.getQuantity();
		double goal = tradeData.getGoal();
		double be = tradeData.getBreakEven();
		double sl = tradeData.getStopLoss();
		double ps = Math.abs(price - sl) / Math.abs(goal - sl) * 100;
		double unrealizedPnl = position.getUnrealizedPnl();

		if (unrealizedPnl == Double.MAX_VALUE)
			unrealizedPnl = 0.0;
		else {
			unrealizedPnl -= TradeCalc.estimatedCommissionIbkrUSD(qtt, price);
		}

		positionRow.setQtt(qtt);
		positionRow.setSl(sl);
		positionRow.setBe(be);
		positionRow.setPs(ps);
		positionRow.setPl(unrealizedPnl);

		if (unrealizedPnl >= 0) {
			positionRow.setPlColor(Color.GREEN.darker());
		} else  {
			positionRow.setPlColor(Color.RED.darker());
		}

		if (price < be) {
			positionRow.setPsColor(Color.RED.darker());
		} else if (price < goal) {
			positionRow.setPsColor(Color.ORANGE.darker());
		} else {
			positionRow.setPsColor(Color.GREEN.darker());
		}
	}
}
