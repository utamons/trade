package com.corn.trade.panel.analysis;

import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.RowPanel;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;
import com.corn.trade.trade.analysis.TradeCalc;
import com.corn.trade.trade.analysis.TradeContext;
import com.corn.trade.trade.analysis.TradeContextData;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ParamPanel extends JPanel {

	public ParamPanel(TradeCalc calc, TradeContext context,
	                  Dimension maxSize,
	                  Dimension minSize, int spacing,
	                  int fieldHeight) {

		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

		this.setPreferredSize(maxSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);

		this.setLayout(new GridLayout(1, 2, 20, 1));

		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		this.setBorder(emptyBorder);

		LabeledComboBox positionBox = new LabeledComboBox("Position:",
		                                                  new String[]{
				                                                  PositionType.LONG.toString(),
				                                                  PositionType.SHORT.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight, null);


		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MIN_GOAL.toString(),
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    null);


		LabeledDoubleField level = new LabeledDoubleField("Level:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         false,
		                                                  null);

		LabeledDoubleField goal = new LabeledDoubleField("Goal:",
		                                                      10,
		                                                      null,
		                                                      spacing,
		                                                      fieldHeight,
		                                                      false,
		                                                      null);


		LabeledDoubleField slippage = new LabeledDoubleField("Slippage:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                     null);


		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                         null);


		LabeledDoubleField price = new LabeledDoubleField("Price:",
		                                                     10,
		                                                     null,
		                                                     spacing,
		                                                     fieldHeight,
		                                                     false,
		                                                     null);


		LabeledDoubleField techStop = new LabeledDoubleField("Tech. stop:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  true,
		                                                  null);


		RowPanel rowPanel = new RowPanel(0);

		JButton calculateButton = new JButton("Calculate");

		calculateButton.addActionListener(e -> {
			TradeContextData data = TradeContextData.TradeContextDataBuilder
					.aTradeContextData()
					.withPositionType(PositionType.valueOf(positionBox.getSelectedItem()))
					.withEstimationType(EstimationType.valueOf(estimationBox.getSelectedItem()))
					.withLevel(level.getValue())
					.withGoal(goal.getValue())
					.withSlippage(slippage.getValue())
					.withPrice(price.getValue())
					.withTechStopLoss(techStop.getValue())
					.withPowerReserve(powerReserve.getValue())
					.build();
			context.setData(data);
			calc.calculate();
		});

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> {
			level.setValue(null);
			goal.setValue(null);
			slippage.setValue(null);
			price.setValue(null);
			techStop.setValue(null);
			powerReserve.setValue(null);
			context.setData(null);
		});

		rowPanel.add(calculateButton);
		rowPanel.add(clearButton);

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
}
