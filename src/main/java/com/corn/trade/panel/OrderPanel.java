package com.corn.trade.panel;

import com.corn.trade.component.RowPanel;
import com.corn.trade.component.LabeledDoubleField;
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

		RowPanel rowPanel = new RowPanel();

		stopLimitBtn = new JButton("Stop-limit");
		marketBtn = new JButton("Market");

		rowPanel.add(stopLimitBtn);
		rowPanel.add(marketBtn);

		this.add(panel, BorderLayout.NORTH);
		this.add(rowPanel, BorderLayout.SOUTH);

		calculator.addUpdater(() -> {
			quantity.setValue(calculator.getQuantity() == null ? null : (double) calculator.getQuantity());
			limit.setValue(calculator.getOrderLimit());
			stop.setValue(calculator.getOrderStop());
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
