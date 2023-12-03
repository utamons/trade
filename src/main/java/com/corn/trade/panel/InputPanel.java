package com.corn.trade.panel;

import com.corn.trade.component.ButtonRowPanel;
import com.corn.trade.component.LabeledComboBox;
import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.component.LabeledLookup;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

import static com.corn.trade.util.Util.log;

public class InputPanel extends BasePanel {

	public InputPanel(Calculator calculator, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Input", calculator, maxSize, minSize);
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		List<String> items = List.of("AAPL", "TSLA", "AMZN", "GOOG", "MSFT");

		LabeledLookup labeledLookup = new LabeledLookup("Ticker", items, spacing, fieldHeight, (ticker) -> log(ticker));


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
		                                                    (value) -> calculator.setEstimationType(EstimationType.fromString(value)));


		calculator.setPositionType(PositionType.fromString(positionBox.getSelectedItem()));
		calculator.setEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()));

		LabeledDoubleField spread = new LabeledDoubleField("Spread:",
		                                                   10,
		                                                   null,
		                                                   spacing,
		                                                   fieldHeight,
		                                                   calculator::setSpread);

		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setPowerReserve);

		LabeledDoubleField level = new LabeledDoubleField("Level:",
		                                                 10,
		                                                 null,
		                                                 spacing,
		                                                 fieldHeight,
		                                                 calculator::setLevel);

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		JButton estimate = new JButton("Estimate");
		JButton reset = new JButton("Reset");

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

		this.add(labeledLookup);
		this.add(positionBox);
		this.add(estimationBox);
		this.add(spread);
		this.add(powerReserve);
		this.add(level);

		this.add(buttonRowPanel);
	}
}
