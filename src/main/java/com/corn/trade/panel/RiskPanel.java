package com.corn.trade.panel;

import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import java.awt.*;

public class RiskPanel extends BasePanel {

	public RiskPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Risk", calculator, maxSize, minSize);

		this.add(new LabeledDoubleField("Risk:", 10, null, spacing, fieldHeight, null));
		this.add(new LabeledDoubleField("Risk (%):", 10, null, spacing, fieldHeight, null));
		this.add(new LabeledDoubleField("R/R:", 10, null, spacing, fieldHeight, null));
	}
}
