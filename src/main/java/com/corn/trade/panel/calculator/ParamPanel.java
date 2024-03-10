package com.corn.trade.panel.calculator;

import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.RowPanel;
import com.corn.trade.panel.BasePanel;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.Levels;
import com.corn.trade.trade.PositionType;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ParamPanel extends BasePanel {

	public ParamPanel(Calculator calculator, Levels levels,
	                  Dimension maxSize,
	                  Dimension minSize, int spacing,
	                  int fieldHeight) {

		super(calculator, null, levels, maxSize, minSize);
		this.setLayout(new GridLayout(1, 2, 20, 1));

		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

		this.setBorder(emptyBorder);

		LabeledComboBox positionBox = new LabeledComboBox("Position:",
		                                                  new String[]{
				                                                  PositionType.LONG.toString(),
				                                                  PositionType.SHORT.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight,
		                                                  (value) -> {
			                                                  calculator.setPositionType(PositionType.fromString(value));
		                                                  });


		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MAX_GAIN_MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_GAIN_MIN_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    (value) -> calculator.setEstimationType(EstimationType.fromString(
				                                                    value)));


		LabeledDoubleField level = new LabeledDoubleField("Level:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         false,
		                                                         levels::setPowerReserve);

		LabeledDoubleField goal = new LabeledDoubleField("Goal:",
		                                                      10,
		                                                      null,
		                                                      spacing,
		                                                      fieldHeight,
		                                                      false,
		                                                      levels::setTempLevel);


		LabeledDoubleField slippage = new LabeledDoubleField("Slippage:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  levels::setPowerReserve);


		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  levels::setPowerReserve);


		LabeledDoubleField price = new LabeledDoubleField("Ask:",
		                                                     10,
		                                                     null,
		                                                     spacing,
		                                                     fieldHeight,
		                                                     false,
		                                                     levels::setPowerReserve);


		RowPanel rowPanel = new RowPanel();

		JButton calculateButton = new JButton("Calculate");
		JButton clearButton = new JButton("Clear");

		rowPanel.add(calculateButton);
		rowPanel.add(clearButton);

		calculator.setPositionType(PositionType.fromString(positionBox.getSelectedItem()));
		calculator.setEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()));

		JPanel leftPanel = new JPanel();
		leftPanel.setMinimumSize(new Dimension(300, 200));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(positionBox);
		leftPanel.add(estimationBox);
		leftPanel.add(level);
		leftPanel.add(goal);

		JPanel rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(300, 200));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(slippage);
		rightPanel.add(powerReserve);
		rightPanel.add(price);
		rightPanel.add(rowPanel);

		add(leftPanel);
		add(rightPanel);
	}
}
