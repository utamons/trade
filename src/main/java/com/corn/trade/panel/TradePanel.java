package com.corn.trade.panel;

import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class TradePanel extends BasePanel {

	public TradePanel(Calculator calculator,Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Trade", calculator, maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		this.add(new LabeledDoubleField("Quantity:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setQuantity));
		this.add(new LabeledDoubleField("Stop Loss:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setStopLoss));
		this.add(new LabeledDoubleField("Take profit:",
		                                10,
		                                null,
		                                spacing,
		                                fieldHeight,
		                                calculator::setTakeProfit));
		this.add(new LabeledDoubleField("Break even:", 10, null, spacing, fieldHeight, null));
	}
}
