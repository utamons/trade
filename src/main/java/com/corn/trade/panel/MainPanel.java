package com.corn.trade.panel;

import com.corn.trade.component.*;
import com.corn.trade.component.position.Position;
import com.corn.trade.component.position.PositionPanel;
import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Ticker;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.jpa.JpaRepo;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.Levels;
import com.corn.trade.trade.PositionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainPanel extends BasePanel {

	private final JCheckBox autoUpdateCheckBox;
	private final JCheckBox autoLevelsCheckBox;

	public MainPanel(Calculator calculator,
	                 AutoUpdate autoUpdate,
	                 Levels levels,
	                 Dimension maxSize,
	                 Dimension minSize,
	                 int spacing,
	                 int fieldHeight) {
		super(calculator, autoUpdate, levels, maxSize, minSize);

		JpaRepo<Exchange, Long> exchangeRepo = new JpaRepo<>(Exchange.class);
		JpaRepo<Ticker, Long>   tickerRepo   = new JpaRepo<>(Ticker.class);

		List<Exchange> exchanges = exchangeRepo.findAll().stream().sorted().toList();
		List<Ticker>   tickers   = tickerRepo.findAll().stream().sorted().toList();

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		List<String> items = tickers.stream().map(Ticker::getName).toList();

		LabeledLookup tickerLookup = new LabeledLookup("Ticker:", items, spacing, fieldHeight, autoUpdate::setTicker);
		autoUpdate.setTickerUpdateSuccessListener(tickerLookup::setSuccessStatus);


		LabeledComboBox exchangeBox = new LabeledComboBox("Exchange:",
		                                                  exchanges.stream().map(Exchange::getName).toArray(String[]::new),
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
		                                                  (value) -> {
			                                                  calculator.setPositionType(PositionType.fromString(value));
			                                                  autoUpdate.setLong(PositionType.fromString(value) ==
			                                                                     PositionType.LONG);
		                                                  });


		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
				                                                    EstimationType.MAX_GAIN_MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_GAIN_MIN_STOP_LOSS.toString(),
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString()
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
		                                                   true,
		                                                   calculator::setSpread);

		LabeledDoubleField powerReserve = new LabeledDoubleField("Power reserve:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         false,
		                                                         levels::setPowerReserve);

		LabeledDoubleField tempLevel = new LabeledDoubleField("Temp. Level:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  levels::setTempLevel);

		LabeledDoubleField bestPrice = new LabeledDoubleField("Best Price:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  false,
		                                                  levels::setBestPrice);

		LabeledDoubleField support = new LabeledDoubleField("Support:",
		                                                    10,
		                                                    null,
		                                                    spacing,
		                                                    fieldHeight,
		                                                    false,
		                                                    levels::setSupport);

		LabeledDoubleField resistance = new LabeledDoubleField("Resistance:",
		                                                       10,
		                                                       null,
		                                                       spacing,
		                                                       fieldHeight,
		                                                       false,
		                                                       levels::setResistance);

		InfoField adr = new InfoField("ADR:", 15, spacing, 5, fieldHeight);
		adr.setInfoText("1.55");
		adr.setBold(true);
		adr.startBlinking(Color.RED, 500);

		TrafficLight trafficLight = new TrafficLight();

		RowPanel rowPanel = new RowPanel();
		JPanel autoLevelsPanel = new JPanel();
		autoLevelsPanel.setLayout(new BorderLayout());

		RowPanel trafficPanel = new RowPanel();
		trafficPanel.add(trafficLight);

		autoUpdateCheckBox = new JCheckBox("Auto-update");
		autoLevelsCheckBox = new JCheckBox("Auto-levels");

		autoLevelsPanel.add(autoLevelsCheckBox, BorderLayout.WEST);

		JButton estimate = new JButton("Estimate");
		JButton reset    = new JButton("Reset");

		autoUpdateCheckBox.addActionListener(e -> {
			levels.setAutoUpdate(autoUpdateCheckBox.isSelected());
			autoUpdate.setAutoUpdate(autoUpdateCheckBox.isSelected());
			calculator.setAutoUpdate(autoUpdateCheckBox.isSelected());
			estimate.setEnabled(!autoUpdateCheckBox.isSelected());
			reset.setEnabled(!autoUpdateCheckBox.isSelected());
			powerReserve.setAutoUpdate(autoUpdateCheckBox.isSelected());
			spread.setEditable(!autoUpdateCheckBox.isSelected());
			bestPrice.setEditable(!autoUpdateCheckBox.isSelected());
			exchangeBox.setEnabled(!autoUpdateCheckBox.isSelected());
			tickerLookup.setEnabled(!autoUpdateCheckBox.isSelected());
		});

		rowPanel.add(autoUpdateCheckBox);
		rowPanel.add(estimate);
		rowPanel.add(reset);

		calculator.addUpdater(() -> {
			spread.setValue(calculator.getSpread());
			spread.setError(calculator.isSpreadError());
		});

		levels.addUpdater(() -> {
			support.setError(levels.isSupportError());
			resistance.setError(levels.isResistanceError());
			tempLevel.setError(levels.isTempLevelError());
			powerReserve.setValue(levels.getPowerReserve());
			powerReserve.setError(levels.isPowerReserveError());
			bestPrice.setError(levels.isBestPriceError());
			bestPrice.setValue(levels.getBestPrice());

			resistance.light(levels.isPivotPointResistance(), Color.GREEN);
			support.light(levels.isPivotPointSupport(), Color.GREEN);
			tempLevel.light(levels.isPivotPointTempLevel(), Color.GREEN);
			bestPrice.light(levels.isPivotPointBestPrice(), Color.GREEN);
		});

		autoUpdate.addUpdater(() -> exchangeBox.setSelectedItem(autoUpdate.getExchange()));

		autoUpdate.addUpdater(() -> {
			if (!autoUpdate.isAutoUpdate()) {
				autoUpdateCheckBox.setSelected(false);
				return;
			}
			bestPrice.setValue(autoUpdate.getBestPrice());
			levels.setBestPrice(autoUpdate.getBestPrice());
			spread.setValue(autoUpdate.getSpread());
			levels.setHighDay(autoUpdate.getHigh());
			levels.setLowDay(autoUpdate.getLow());
			if (autoLevelsCheckBox.isSelected()) {
				resistance.setValue(autoUpdate.getHigh());
				support.setValue(autoUpdate.getLow());
			}
			calculator.setSpread(autoUpdate.getSpread());
			levels.calculatePivotPoint(calculator.getPositionType());
			levels.calculatePowerReserve(calculator.getPositionType());
			calculator.estimate();
		});

		estimate.addActionListener(e -> calculator.estimate());
		reset.addActionListener(e -> {
			calculator.reset();
			levels.reset();
		});

		PositionPanel position = new PositionPanel();

		panel.add(exchangeBox);
		panel.add(tickerLookup);
		panel.add(positionBox);
		panel.add(estimationBox);
		panel.add(spread);
		panel.add(powerReserve);
		panel.add(bestPrice);
		panel.add(resistance);
		panel.add(tempLevel);
		panel.add(support);
		panel.add(adr);
		panel.add(trafficPanel);
		panel.add(autoLevelsPanel);
		panel.add(position);

		position.addPosition("AAPL");
		position.addPosition("MSFT");

		this.add(panel, BorderLayout.NORTH);

		this.add(rowPanel, BorderLayout.SOUTH);
	}
}