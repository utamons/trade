package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;

import javax.swing.*;
import java.awt.*;

public class OrderPanel extends BasePanel {

	private final JButton stopLimitBtn;
	private final JButton marketBtn;
	private final JButton closeAllBtn;

	public OrderPanel(Calculator calculator, AutoUpdate autoUpdate, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Order", calculator, autoUpdate, maxSize, minSize);

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

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		stopLimitBtn = new JButton("Stop-limit");
		marketBtn = new JButton("Market");
		closeAllBtn = new JButton("Close All");
		closeAllBtn.setBackground(new Color(100, 0, 0));

		buttonRowPanel.add(stopLimitBtn);
		buttonRowPanel.add(marketBtn);
		buttonRowPanel.add(closeAllBtn);

		this.add(panel, BorderLayout.NORTH);
		this.add(buttonRowPanel, BorderLayout.SOUTH);

		calculator.addTrigger(() -> {
			quantity.setValue(calculator.getQuantity() == null ? null : (double) calculator.getQuantity());
			limit.setValue(calculator.getOrderLimit());
			stop.setValue(calculator.getOrderStop());
		});
	}

	public void enableOrderButtons(boolean enabled) {
		stopLimitBtn.setEnabled(enabled);
		marketBtn.setEnabled(enabled);
		closeAllBtn.setEnabled(enabled);
	}
}
