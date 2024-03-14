package com.corn.trade.panel.analysis;

import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.RowPanel;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;
import com.corn.trade.trade.analysis.TradeData;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

import static com.corn.trade.BaseWindow.ORDER_LUFT;

public class ParamPanel extends JPanel {

	private final LabeledComboBox positionBox;
	private final LabeledComboBox estimationBox;
	private final LabeledDoubleField level;
	private final LabeledDoubleField goal;
	private final LabeledDoubleField slippage;
	private final LabeledDoubleField powerReserve;
	private final LabeledDoubleField price;
	private final LabeledDoubleField techStop;

	public ParamPanel(Dimension maxSize,
	                  Dimension minSize,
	                  int spacing,
	                  int fieldHeight,
	                  Consumer<TradeData> consumer
	                  ) {

		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);

		this.setPreferredSize(maxSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);

		this.setLayout(new GridLayout(1, 2, 20, 1));

		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		this.setBorder(emptyBorder);

		positionBox = new LabeledComboBox("Position:",
		                                                  new String[]{
				                                                  PositionType.LONG.toString(),
				                                                  PositionType.SHORT.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight, null);


		estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_GOAL.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    null);


		level = new LabeledDoubleField("Level:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  null);

		goal = new LabeledDoubleField("Goal:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 false,
		                                                 null);


		slippage = new LabeledDoubleField("Slippage:",
		                                                     10,
		                                                     null,
		                                                     spacing,
		                                                     fieldHeight,
		                                                     false,
		                                                     null);


		powerReserve = new LabeledDoubleField("Power reserve:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         false,
		                                                         null);


		price = new LabeledDoubleField("Price:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  null);


		techStop = new LabeledDoubleField("Tech. stop:",
		                                                     10,
		                                                     null,
		                                                     spacing,
		                                                     fieldHeight,
		                                                     true,
		                                                     null);


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
			                              .withGoal(goal.getValue())
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

	public void populate(TradeData tradeData) {
		positionBox.setSelectedItem(tradeData.getPositionType().toString());
		estimationBox.setSelectedItem(tradeData.getEstimationType().toString());
		level.setValue(tradeData.getLevel());
		goal.setValue(tradeData.getGoal());
		slippage.setValue(tradeData.getSlippage());
		powerReserve.setValue(tradeData.getPowerReserve());
		price.setValue(tradeData.getPrice());
		techStop.setValue(tradeData.getTechStopLoss());
	}
}
