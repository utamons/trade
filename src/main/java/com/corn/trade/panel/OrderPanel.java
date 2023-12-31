package com.corn.trade.panel;

import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.RowPanel;
import com.corn.trade.component.TrafficLight;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.ibkr.OrderHelper;
import com.corn.trade.ibkr.PositionHelper;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;

import javax.swing.*;
import java.awt.*;

public class OrderPanel extends BasePanel {

	private final JButton stopLimitBtn;

	private final JButton limitBtn;

	public OrderPanel(Calculator calculator,
	                  AutoUpdate autoUpdate,
	                  OrderHelper orderHelper,
	                  PositionHelper positionHelper,
	                  Levels levels,
	                  Dimension maxSize,
	                  Dimension minSize,
	                  int spacing,
	                  int fieldHeight) {
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

		RowPanel     buttonPanel  = new RowPanel();
		RowPanel     trafficPanel = new RowPanel();
		TrafficLight trafficLight = new TrafficLight();
		trafficPanel.add(trafficLight);

		stopLimitBtn = new JButton("Stop-Limit");
		JButton dropAllBtn = new JButton("Drop All");
		limitBtn = new JButton("Limit");

		buttonPanel.add(limitBtn);
		buttonPanel.add(stopLimitBtn);
		buttonPanel.add(dropAllBtn);

		limitBtn.addActionListener(e -> orderHelper.placeOrder(autoUpdate.getContractDetails(),
	                                                       calculator.getQuantity(),
	                                                       null,
	                                                       limit.getValue(),
	                                                       calculator.getStopLoss(),
	                                                       calculator.getTakeProfit(),
	                                                       calculator.getPositionType()));

		stopLimitBtn.addActionListener(e -> orderHelper.placeOrder(autoUpdate.getContractDetails(),
	                                                           calculator.getQuantity(),
	                                                           stop.getValue(),
	                                                           limit.getValue(),
	                                                           calculator.getStopLoss(),
	                                                           calculator.getTakeProfit(),
	                                                           calculator.getPositionType()));

		dropAllBtn.addActionListener(e -> {
			orderHelper.dropAll(positionHelper); // First drop all orders
		});

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
			} else if (calculator.isYellowLight()) {
				trafficLight.setYellow();
				enableOrderButtons(true);
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
		limitBtn.setEnabled(enabled);
	}
}
