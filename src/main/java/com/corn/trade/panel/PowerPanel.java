package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Power", calculator, maxSize, minSize);

		LabeledDoubleField atr = new LabeledDoubleField("ATR (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setAtr);

		LabeledDoubleField high = new LabeledDoubleField("High (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setHighDay);

		LabeledDoubleField low = new LabeledDoubleField("Low (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setLowDay);

		this.add(atr);
		this.add(high);
		this.add(low);

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		JButton button = new JButton("Calculate P/R");

		button.addActionListener(e -> calculator.calculatePowerReserve());

		buttonRowPanel.add(button);

		this.add(buttonRowPanel);

		calculator.addTrigger(() -> {
			atr.setValue(calculator.getAtr());
			high.setValue(calculator.getHighDay());
			low.setValue(calculator.getLowDay());
			atr.setError(calculator.isAtrError());
			high.setError(calculator.isHighDayError());
			low.setError(calculator.isLowDayError());
		});
	}
}
