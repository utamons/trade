package com.corn.trade.panel;

import com.corn.trade.component.LabeledTextField;

import javax.swing.*;
import java.awt.*;

public class TradePanel extends BasePanel {

	public TradePanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Trade", maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		this.add(new LabeledTextField("Quantity:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Stop Loss:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Take profit:", 10, null, spacing, fieldHeight));
		this.add(new LabeledTextField("Break even:", 10, null, spacing, fieldHeight));
	}
}
