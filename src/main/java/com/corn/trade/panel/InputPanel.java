package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends BasePanel {

	public InputPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Input", calculator, maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);


		LabeledComboBox positionBox = new LabeledComboBox("Position:", new String[]{"Long", "Short"}, spacing, fieldHeight);
		this.add(positionBox);

		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    "Max. stop loss.",
				                                                    "Max. take profit."
		                                                    },
		                                                    spacing,
		                                                    fieldHeight);
		this.add(estimationBox);

		LabeledDoubleField spread = new LabeledDoubleField("Spread:",
		                                                   10,
		                                                   null,
		                                                   spacing,
		                                                   fieldHeight,
		                                                   calculator::setSpread);
		this.add(spread);
		this.add(new LabeledDoubleField("Power reserve:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setPowerReserve));
		this.add(new LabeledDoubleField("Level:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setLevel));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Estimate"));
		buttonRowPanel.add(new JButton("Reset"));

		calculator.addTrigger(() -> spread.setValue(calculator.getSpread()));

		this.add(buttonRowPanel);
	}
}
