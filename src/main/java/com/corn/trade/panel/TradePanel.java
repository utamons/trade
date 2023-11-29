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

		LabeledDoubleField quantity = new LabeledDoubleField("Quantity:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setQuantity);

		LabeledDoubleField stopLoss = new LabeledDoubleField("Stop Loss:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setStopLoss);

		LabeledDoubleField take = new LabeledDoubleField("Take profit:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setTakeProfit);

		LabeledDoubleField breakEven = new LabeledDoubleField("Break even:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 null);
		this.add(breakEven);
		this.add(quantity);
		this.add(stopLoss);
		this.add(take);

		calculator.addTrigger(
				() -> {
					breakEven.setValue(calculator.getBreakEven());
					quantity.setValue(calculator.getQuantity() == null ? null : (double) calculator.getQuantity());
					stopLoss.setValue(calculator.getStopLoss());
					take.setValue(calculator.getTakeProfit());
				}
		);
	}
}
