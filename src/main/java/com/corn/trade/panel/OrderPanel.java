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

@Deprecated
public class OrderPanel extends BasePanel {

	public static final int BUTTON_DELAY = 5000;
	private final JButton stopLimitBtn;
	private final JCheckBox allowYellowCheckBox;

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
		autoUpdate.addActivateListener(quantity::setControlCheckBoxState);

		JPanel checkBoxPanel = new JPanel();
		allowYellowCheckBox = new JCheckBox("Allow Yellow");
		// Switch it off after 10 minutes
		Timer allowYellowTimer = new Timer(600_000, evt -> {
			allowYellowCheckBox.setSelected(false);
			allowYellowCheckBox.setForeground((Color) UIManager.get("TextField.foreground"));
		});
		allowYellowTimer.setRepeats(false); // Make sure the timer only runs once

		allowYellowCheckBox.addActionListener(e -> {
			if (allowYellowCheckBox.isSelected()) {
				allowYellowCheckBox.setForeground(Color.RED);

				allowYellowTimer.start();
			} else {
				allowYellowCheckBox.setForeground((Color) UIManager.get("TextField.foreground"));
				allowYellowTimer.stop();
			}
		});

		checkBoxPanel.setLayout(new BorderLayout());
		checkBoxPanel.add(allowYellowCheckBox, BorderLayout.WEST);

		panel.add(checkBoxPanel);
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

		Timer buttonDelayTimer = new Timer(BUTTON_DELAY, evt -> {
			limitBtn.setEnabled(true);
			stopLimitBtn.setEnabled(true);
		});
		buttonDelayTimer.setRepeats(false);

		limitBtn.addActionListener(e -> {
			limitBtn.setEnabled(false);
			stopLimitBtn.setEnabled(false);

			if (limit.getValue() != null) {
				orderHelper.placeOrder(autoUpdate.getContractDetails(),
				                       calculator.getQuantity(),
				                       null,
				                       limit.getValue(),
				                       calculator.getCorrectedStopLoss(),
				                       calculator.getTakeProfit(),
				                       calculator.getBreakEven(),
				                       calculator.getPositionType());
			}

			buttonDelayTimer.start();
		});

		stopLimitBtn.addActionListener(e -> {
			limitBtn.setEnabled(false);
			stopLimitBtn.setEnabled(false);

			if (stop.getValue() != null && limit.getValue() != null) {
				orderHelper.placeOrder(autoUpdate.getContractDetails(),
				                       calculator.getQuantity(),
				                       stop.getValue(),
				                       limit.getValue(),
				                       calculator.getCorrectedStopLoss(),
				                       calculator.getTakeProfit(),
				                       calculator.getBreakEven(),
				                       calculator.getPositionType());
			}

			buttonDelayTimer.start();
		});

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
				stopLimitBtn.setEnabled(false);
			} else if (calculator.isYellowLight()) {
				trafficLight.setYellow();
				stopLimitBtn.setEnabled(allowYellowCheckBox.isSelected());
			} else {
				trafficLight.setGreen();
				stopLimitBtn.setEnabled(true);
			}
			limitBtn.setEnabled(!levels.isPivotPointALevel() && !calculator.isTradeError() && (!calculator.isYellowLight() || allowYellowCheckBox.isSelected()));
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
}
