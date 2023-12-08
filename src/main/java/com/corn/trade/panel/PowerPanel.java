package com.corn.trade.panel;

import com.corn.trade.component.RowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;

import javax.swing.*;
import java.awt.*;

public class PowerPanel extends BasePanel {

	public PowerPanel(Calculator calculator,
	                  AutoUpdate autoUpdate,
					  Levels levels,
	                  Dimension maxSize,
	                  Dimension minSize,
	                  int spacing,
	                  int fieldHeight) {
		super("Power", calculator, autoUpdate, levels, maxSize, minSize);
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
			                                                levels.setAtr(value);
			                                                autoUpdate.setAtr(value);
		                                                });

		LabeledDoubleField high = new LabeledDoubleField("High (day):",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 false,
		                                                 levels::setHighDay);

		LabeledDoubleField low = new LabeledDoubleField("Low (day):",
		                                                10,
		                                                null,
		                                                spacing,
		                                                fieldHeight,
		                                                false,
		                                                levels::setLowDay);

		panel.add(atr);
		panel.add(high);
		panel.add(low);

		RowPanel rowPanel = new RowPanel();

		JButton button = new JButton("Calculate P/R");

		button.addActionListener(e -> {
			levels.calculatePivotPoint(calculator.getPositionType());
			levels.calculatePowerReserve(calculator.getPositionType());
		});

		autoUpdate.addActivateListener(
				(isAutoUpdate) -> {
					button.setEnabled(!isAutoUpdate);
					high.setAutoUpdate(isAutoUpdate);
					low.setAutoUpdate(isAutoUpdate);
				});

		autoUpdate.addUpdater(() -> {
			if (autoUpdate.isAutoUpdate()) {
				high.setValue(autoUpdate.getHigh());
				low.setValue(autoUpdate.getLow());
			}
		});

		levels.addUpdater(() -> {
			atr.setValue(levels.getAtr());
			high.setValue(levels.getHighDay());
			low.setValue(levels.getLowDay());
			atr.setError(levels.isAtrError());
			high.setError(levels.isHighDayError());
			low.setError(levels.isLowDayError());
		});

		rowPanel.add(button);

		this.add(panel, BorderLayout.NORTH);
		this.add(rowPanel, BorderLayout.SOUTH);
	}
}
