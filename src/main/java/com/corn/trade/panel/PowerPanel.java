package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Calculator calculator, AutoUpdate autoUpdate, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Power", calculator, autoUpdate, maxSize, minSize);
		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		LabeledDoubleField atr = new LabeledDoubleField("ATR (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
														 false,
		                                                 calculator::setAtr);

		LabeledDoubleField high = new LabeledDoubleField("High (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
														 false,
		                                                 calculator::setHighDay);

		LabeledDoubleField low = new LabeledDoubleField("Low (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
														 false,
		                                                 calculator::setLowDay);

		panel.add(atr);
		panel.add(high);
		panel.add(low);

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		JButton button = new JButton("Calculate P/R");

		button.addActionListener(e -> calculator.calculatePowerReserve());
		autoUpdate.addActivateListener((isAutoUpdate) -> button.setEnabled(!isAutoUpdate));

		buttonRowPanel.add(button);

		this.add(panel, BorderLayout.NORTH);
		this.add(buttonRowPanel, BorderLayout.SOUTH);

		calculator.addUpdater(() -> {
			atr.setValue(calculator.getAtr());
			high.setValue(calculator.getHighDay());
			low.setValue(calculator.getLowDay());
			atr.setError(calculator.isAtrError());
			high.setError(calculator.isHighDayError());
			low.setError(calculator.isLowDayError());
		});
	}
}
