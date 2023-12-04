package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.LabeledLookup;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InputPanel extends BasePanel {

	private final JCheckBox autoUpdateCheckBox;

	public InputPanel(Calculator calculator, AutoUpdate autoUpdate, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Input", calculator, autoUpdate, maxSize, minSize);
		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		List<String> items = List.of("AAPL", "TSLA", "AMZN", "GOOG", "MSFT");

		LabeledLookup tickerLookup = new LabeledLookup("Ticker:", items, spacing, fieldHeight, autoUpdate::setTicker);
		autoUpdate.setTickerUpdateSuccessListener(tickerLookup::setSuccessStatus);

		LabeledComboBox exchangeBox = new LabeledComboBox("Exchange:",
		                                                  new String[]{
				                                                  ExchangeType.NASDAQ.toString(),
				                                                  ExchangeType.NYSE.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight,
		                                                  autoUpdate::setExchange);

		exchangeBox.setSelectedItem(autoUpdate.getExchange());

		LabeledComboBox positionBox = new LabeledComboBox("Position:",
		                                                  new String[]{
				                                                  PositionType.LONG.toString(),
				                                                  PositionType.SHORT.toString()
		                                                  },
		                                                  spacing,
		                                                  fieldHeight,
		                                                  (value) -> calculator.setPositionType(PositionType.fromString(value)));


		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_GAIN_MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_GAIN_MIN_STOP_LOSS.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    (value) -> calculator.setEstimationType(EstimationType.fromString(
				                                                    value)));


		calculator.setPositionType(PositionType.fromString(positionBox.getSelectedItem()));
		calculator.setEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()));

		LabeledDoubleField spread = new LabeledDoubleField("Spread:",
		                                                   10,
		                                                   null,
		                                                   spacing,
		                                                   fieldHeight,
		                                                   autoUpdate.isAutoUpdate(),
		                                                   calculator::setSpread);
		autoUpdate.addActivateListener(spread::setAutoSwitchVisible);

		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         autoUpdate.isAutoUpdate(),
		                                                         calculator::setPowerReserve);

		autoUpdate.addActivateListener(powerReserve::setAutoSwitchVisible);

		LabeledDoubleField level = new LabeledDoubleField("Temp. Level:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  calculator::setLevel);

		LabeledDoubleField price = new LabeledDoubleField("Price:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  autoUpdate.isAutoUpdate(),
		                                                  calculator::setPrice);

		autoUpdate.addActivateListener(price::setAutoSwitchVisible);

		LabeledDoubleField support = new LabeledDoubleField("Min.take (support):",
		                                                    10,
		                                                    null,
		                                                    spacing,
		                                                    fieldHeight,
															autoUpdate.isAutoUpdate(),
		                                                    calculator::setMinTake);

		autoUpdate.addActivateListener(support::setAutoSwitchVisible);

		LabeledDoubleField resistance = new LabeledDoubleField("Max take (resistance):",
		                                                       10,
		                                                       null,
		                                                       spacing,
		                                                       fieldHeight,
															   autoUpdate.isAutoUpdate(),
		                                                       calculator::setMaxTake);

		autoUpdate.addActivateListener(resistance::setAutoSwitchVisible);

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		autoUpdateCheckBox = new JCheckBox("Auto-update");

		JButton estimate = new JButton("Estimate");
		JButton reset    = new JButton("Reset");

		autoUpdateCheckBox.addActionListener(e -> {
			autoUpdate.setAutoUpdate(autoUpdateCheckBox.isSelected());
			estimate.setEnabled(!autoUpdateCheckBox.isSelected());
			reset.setEnabled(!autoUpdateCheckBox.isSelected());
		});

		buttonRowPanel.add(autoUpdateCheckBox);
		buttonRowPanel.add(estimate);
		buttonRowPanel.add(reset);

		calculator.addTrigger(() -> {
			spread.setValue(calculator.getSpread());
			level.setValue(calculator.getLevel());
			powerReserve.setValue(calculator.getPowerReserve());
			spread.setError(calculator.isSpreadError());
			level.setError(calculator.isLevelError());
			powerReserve.setError(calculator.isPowerReserveError());
		});

		estimate.addActionListener(e -> calculator.estimate());
		reset.addActionListener(e -> calculator.reset());

		panel.add(exchangeBox);
		panel.add(tickerLookup);
		panel.add(positionBox);
		panel.add(estimationBox);
		panel.add(spread);
		panel.add(powerReserve);
		panel.add(price);
		panel.add(level);
		panel.add(support);
		panel.add(resistance);

		this.add(panel, BorderLayout.NORTH);
		this.add(buttonRowPanel, BorderLayout.SOUTH);
	}

	public void enableAutoUpdateCheckBox(boolean enabled) {
		autoUpdateCheckBox.setEnabled(enabled);
	}
}
