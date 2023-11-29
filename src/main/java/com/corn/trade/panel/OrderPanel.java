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

		LabeledDoubleField limit = new LabeledDoubleField("Limit:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 null);

		LabeledDoubleField stop = new LabeledDoubleField("Stop:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 null);
		this.add(limit);
		this.add(stop);

		calculator.addTrigger(() -> {
			limit.setValue(calculator.getOrderLimit());
			stop.setValue(calculator.getOrderStop());
		});
	}
}
