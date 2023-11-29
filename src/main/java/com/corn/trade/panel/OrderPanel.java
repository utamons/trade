package com.corn.trade.panel;

import com.corn.trade.component.LabeledTextField;

import javax.swing.*;
import java.awt.*;

public class OrderPanel extends BasePanel {

	public OrderPanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Order", maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		this.add(new LabeledTextField("Limit:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Stop:", 10, null, spacing, fieldHeight));
	}
}
