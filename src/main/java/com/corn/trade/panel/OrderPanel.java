package com.corn.trade.panel;

import com.corn.trade.component.RowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.TrafficLight;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;

import javax.swing.*;
import java.awt.*;

public class OrderPanel extends BasePanel {

	private final JButton stopLimitBtn;
	private final JButton marketBtn;
	public OrderPanel(Calculator calculator, AutoUpdate autoUpdate, Levels levels, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Order", calculator, autoUpdate, levels, maxSize, minSize);

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		LabeledDoubleField limit = new LabeledDoubleField("Limit:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
														 false,
		                                                 null);

		LabeledDoubleField stop = new LabeledDoubleField("Stop:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
														 false,
		                                                 null);

		LabeledDoubleField quantity = new LabeledDoubleField("Quantity:",
		                                                     10,
		                                                     null,
		                                                     spacing,
		                                                     fieldHeight,
		                                                     autoUpdate.isAutoUpdate(),
		                                                     calculator::setQuantity);
		autoUpdate.addActivateListener(quantity::setAutoSwitchVisible);

		panel.add(quantity);
		panel.add(limit);
		panel.add(stop);

		RowPanel buttonPanel = new RowPanel();
		RowPanel trafficPanel = new RowPanel();
		TrafficLight trafficLight = new TrafficLight();
		trafficPanel.add(trafficLight);

		stopLimitBtn = new JButton("Stop-limit");
		marketBtn = new JButton("Market");

		buttonPanel.add(stopLimitBtn);
		buttonPanel.add(marketBtn);

		this.add(panel, BorderLayout.NORTH);
		this.add(trafficPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);

		calculator.addUpdater(() -> {
			quantity.setValue(calculator.getQuantity() == null ? null : (double) calculator.getQuantity());
			limit.setValue(calculator.getOrderLimit());
			stop.setValue(calculator.getOrderStop());
			if (calculator.isTradeError()) {
				trafficLight.setRed();
				enableOrderButtons(false);
			} else {
				trafficLight.setGreen();
				enableOrderButtons(true);
			}
		});

		autoUpdate.addActivateListener(
				(isAutoUpdate) -> {
					if (isAutoUpdate) {
						limit.setEditable(false);
						stop.setEditable(false);
					} else {
						limit.setEditable(true);
						stop.setEditable(true);
					}
				});
	}

	public void enableOrderButtons(boolean enabled) {
		stopLimitBtn.setEnabled(enabled);
		marketBtn.setEnabled(enabled);
	}
}
