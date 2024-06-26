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
package com.corn.trade.ui.view;

import com.corn.trade.ui.component.LabeledComboBox;
import com.corn.trade.ui.component.LabeledDoubleField;
import com.corn.trade.ui.component.RowPanel;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
import com.corn.trade.util.Trigger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

import static com.corn.trade.BaseWindow.ORDER_LUFT;

public class ParamPanel extends JPanel {

	private final LabeledComboBox    positionBox;
	private final LabeledComboBox    estimationBox;
	private final LabeledDoubleField level;
	private final LabeledDoubleField goal;
	private final LabeledDoubleField slippage;
	private final LabeledDoubleField powerReserve;
	private final LabeledDoubleField price;
	private final LabeledDoubleField techStop;
	private       Trigger            onClear;

	public ParamPanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight, Consumer<TradeData> consumer) {

		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

		this.setPreferredSize(maxSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);

		this.setLayout(new GridLayout(1, 2, 20, 1));

		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		this.setBorder(emptyBorder);

		positionBox = new LabeledComboBox("Position:", PositionType.getValues(), spacing, fieldHeight);
		estimationBox = new LabeledComboBox("Estimation:", EstimationType.getValues(), spacing, fieldHeight);
		level = new LabeledDoubleField("Level:", 10, spacing, fieldHeight, false);
		goal = new LabeledDoubleField("Goal:", 10, spacing, fieldHeight, false);
		slippage = new LabeledDoubleField("Slippage:", 10, spacing, fieldHeight, false);
		powerReserve = new LabeledDoubleField("Power reserve:", 10, spacing, fieldHeight, false);
		price = new LabeledDoubleField("Price:", 10, spacing, fieldHeight, false);
		techStop = new LabeledDoubleField("Tech. stop:", 10, spacing, fieldHeight, true);

		RowPanel rowPanel = getRowPanel(consumer);

		JPanel leftPanel = new JPanel();
		leftPanel.setMinimumSize(new Dimension(300, 350));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(positionBox);
		leftPanel.add(estimationBox);
		leftPanel.add(level);
		leftPanel.add(goal);

		JPanel rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(300, 350));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(slippage);
		rightPanel.add(powerReserve);
		rightPanel.add(price);
		rightPanel.add(techStop);
		rightPanel.add(rowPanel);

		add(leftPanel);
		add(rightPanel);
	}

	private RowPanel getRowPanel(Consumer<TradeData> consumer) {
		RowPanel rowPanel = new RowPanel(0);

		JButton calculateButton = new JButton("Calculate");

		calculateButton.addActionListener(e -> {
			PositionType positionType = PositionType.fromString(positionBox.getSelectedItem());

			Double slippageValue = slippage.getValue() == null ? ORDER_LUFT : slippage.getValue();
			slippage.setValue(slippageValue);

			if (estimationBox.getSelectedItem().equals(EstimationType.MIN_GOAL.toString())) {
				goal.setValue(null);
				powerReserve.setValue(null);
			}

			TradeData tradeData = TradeData.aTradeData()
			                               .withEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()))
			                               .withPositionType(positionType)
			                               .withPowerReserve(powerReserve.getValue())
			                               .withPrice(price.getValue())
			                               .withLevel(level.getValue())
			                               .withTechStopLoss(techStop.getValue())
			                               .withSlippage(slippage.getValue())
			                               .withTarget(goal.getValue())
			                               .withLuft(ORDER_LUFT)
			                               .build();

			consumer.accept(tradeData);
		});

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> {
			level.setValue(null);
			goal.setValue(null);
			slippage.setValue(null);
			price.setValue(null);
			techStop.setValue(null);
			powerReserve.setValue(null);
			if (onClear != null) {
				onClear.trigger();
			}
		});

		rowPanel.add(calculateButton);
		rowPanel.add(clearButton);
		return rowPanel;
	}

	public void populate(TradeData tradeData) {
		positionBox.setSelectedItem(tradeData.getPositionType().toString());
		estimationBox.setSelectedItem(tradeData.getEstimationType().toString());
		level.setValue(tradeData.getLevel());
		goal.setValue(tradeData.getTarget());
		slippage.setValue(tradeData.getSlippage());
		powerReserve.setValue(tradeData.getPowerReserve());
		price.setValue(tradeData.getPrice());
		techStop.setValue(tradeData.getTechStopLoss());
	}

	public void onClear(Trigger onClear) {
		this.onClear = onClear;
	}
}
