package com.corn.trade.desktop.panel;

import com.corn.trade.desktop.component.LabeledTextField;

import java.awt.*;

public class RiskPanel extends BasePanel {

	public RiskPanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Risk", maxSize, minSize);

		this.add(new LabeledTextField("Risk:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Risk (%):", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("R/R:", 10, null, spacing, fieldHeight));
	}
}
