package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Power", calculator, maxSize, minSize);

		this.add(new LabeledDoubleField("ATR (day):",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setAtr));
		this.add(new LabeledDoubleField("High (day):",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setHighDay));
		this.add(new LabeledDoubleField("Low (day):",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setLowDay));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Caclulate P/R"));

		this.add(buttonRowPanel);
	}
}
