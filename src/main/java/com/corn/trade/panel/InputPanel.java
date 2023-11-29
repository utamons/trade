package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledTextField;

import javax.swing.*;
import java.awt.*;

public class InputPanel extends BasePanel {

	public InputPanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Input", maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);


		LabeledComboBox positionBox = new LabeledComboBox("Position:", new String[]{"Long", "Short"}, spacing, fieldHeight);
		this.add(positionBox);

		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    "Max. stop loss.",
				                                                    "Max. take profit.",
				                                                    "Min. break even."},
		                                                    spacing,
		                                                    fieldHeight);
		this.add(estimationBox);

		this.add(new LabeledTextField("Spread:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Power reserve:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Level:", 10, null, spacing, fieldHeight));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Estimate"));
		buttonRowPanel.add(new JButton("Reset"));

		this.add(buttonRowPanel);
	}
}
