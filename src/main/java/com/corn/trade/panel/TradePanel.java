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
		LabeledDoubleField outputEx = new LabeledDoubleField("Output ex.:",
		                                                      10,
		                                                      null,
		                                                      spacing,
		                                                      fieldHeight,
		                                                      null);
		LabeledDoubleField gain = new LabeledDoubleField("Gain:",
		                                                      10,
		                                                      null,
		                                                      spacing,
		                                                      fieldHeight,
		                                                      null);
		this.add(breakEven);
		this.add(stopLoss);
		this.add(take);
		this.add(outputEx);
		this.add(gain);

		calculator.addTrigger(
				() -> {
					breakEven.setValue(calculator.getBreakEven());
					stopLoss.setValue(calculator.getStopLoss());
					take.setValue(calculator.getTakeProfit());
					outputEx.setValue(calculator.getOutputExpected());
					gain.setValue(calculator.getGain());
				}
		);
	}
}
