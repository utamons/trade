package com.corn.trade.panel;

import com.corn.trade.component.RowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Calculator calculator,
	                  AutoUpdate autoUpdate,
	                  Dimension maxSize,
	                  Dimension minSize,
	                  int spacing,
	                  int fieldHeight) {
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
		                                                (value) -> {
			                                                calculator.setAtr(value);
			                                                autoUpdate.setAtr(value);
		                                                });

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

		RowPanel rowPanel = new RowPanel();

		JButton button = new JButton("Calculate P/R");

		button.addActionListener(e -> calculator.calculatePowerReserve());
		autoUpdate.addActivateListener(
				(isAutoUpdate) -> {
					button.setEnabled(!isAutoUpdate);
					high.setAutoUpdate(isAutoUpdate);
					low.setAutoUpdate(isAutoUpdate);
				});

		autoUpdate.addUpdater(() -> {
			high.setValue(autoUpdate.getHigh());
			low.setValue(autoUpdate.getLow());
		});

		calculator.addUpdater(() -> {
			high.setError(calculator.isHighDayError());
			low.setError(calculator.isLowDayError());
		});

		rowPanel.add(button);

		this.add(panel, BorderLayout.NORTH);
		this.add(rowPanel, BorderLayout.SOUTH);
	}
}
