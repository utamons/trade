package com.corn.trade.panel;

import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class OrderPanel extends BasePanel {

	public OrderPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Order", calculator, maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		this.add(new LabeledDoubleField("Limit:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                null));
		this.add(new LabeledDoubleField("Stop:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                null));
	}
}
