package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends BasePanel {

	public InputPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Input", calculator, maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);


		LabeledComboBox positionBox = new LabeledComboBox("Position:",
		                                                  new String[]{
				                                                  PositionType.LONG.toString(),
				                                                  PositionType.SHORT.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight,
		                                                  (value) -> calculator.setPositionType(PositionType.fromString(value)));


		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_TAKE_PROFIT.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    (value) -> calculator.setEstimationType(EstimationType.fromString(value)));
		this.add(positionBox);
		this.add(estimationBox);

		calculator.setPositionType(PositionType.fromString(positionBox.getSelectedItem()));
		calculator.setEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()));

		LabeledDoubleField spread = new LabeledDoubleField("Spread:",
		                                                   10,
		                                                   null,
		                                                   spacing,
		                                                   fieldHeight,
		                                                   calculator::setSpread);
		this.add(spread);
		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setPowerReserve);
		this.add(powerReserve);
		this.add(new LabeledDoubleField("Level:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setLevel));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Estimate"));
		buttonRowPanel.add(new JButton("Reset"));

		calculator.addTrigger(() -> powerReserve.setValue(calculator.getPowerReserve()));

		this.add(buttonRowPanel);
	}
}
