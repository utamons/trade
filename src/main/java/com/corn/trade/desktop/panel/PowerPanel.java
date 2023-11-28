package com.corn.trade.desktop.panel;

import com.corn.trade.desktop.component.ButtonRowPanel;
import com.corn.trade.desktop.component.LabeledComboBox;
import com.corn.trade.desktop.component.LabeledTextField;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Power", maxSize, minSize);

		this.add(new LabeledTextField("ATR (day):", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("High (day):", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Low (day):", 10, null, spacing, fieldHeight));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Caclulate P/R"));

		this.add(buttonRowPanel);
	}
}
