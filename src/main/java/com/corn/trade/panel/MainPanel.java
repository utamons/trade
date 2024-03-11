package com.corn.trade.panel;

import com.corn.trade.component.*;
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
				                                                    EstimationType.MIN_GOAL.toString(),
				                                                    EstimationType.MAX_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_STOP_LOSS.toString(),
				                                                    EstimationType.MIN_FIBO_GOAL.toString()
		                                                    },
		                                                    spacing,
		                                                    fieldHeight,
		                                                    (value) -> calculator.setEstimationType(EstimationType.fromString(
				                                                    value)));


		calculator.setPositionType(PositionType.fromString(positionBox.getSelectedItem()));
		calculator.setEstimationType(EstimationType.fromString(estimationBox.getSelectedItem()));

		LabeledDoubleField level = new LabeledDoubleField("Level:",
		                                                   10,
		                                                   null,
		                                                   spacing,
		                                                   fieldHeight,
		                                                   false,
		                                                   calculator::setSpread);

		LabeledDoubleField goal = new LabeledDoubleField("Goal:",
		                                                         10,
		                                                         null,
		                                                         spacing,
		                                                         fieldHeight,
		                                                         false,
		                                                         levels::setPowerReserve);

		LabeledDoubleField techSL = new LabeledDoubleField("Tech. SL:",
		                                                  10,
		                                                  null,
		                                                  spacing,
		                                                  fieldHeight,
		                                                  true,
		                                                  levels::setTempLevel);


		TrafficLight trafficLight = new TrafficLight();

		RowPanel orderPanel = new RowPanel(20);

		JButton lock = new JButton("Lock");
		JButton stopLimit    = new JButton("Stop-Limit");
		JButton limit = new JButton("Limit");

		orderPanel.add(lock);
		orderPanel.add(trafficLight);
		orderPanel.add(stopLimit);
		orderPanel.add(limit);

		calculator.addUpdater(() -> {
			level.setValue(calculator.getSpread());
			level.setError(calculator.isSpreadError());
		});

		levels.addUpdater(() -> {
			techSL.setError(levels.isTempLevelError());
			goal.setValue(levels.getPowerReserve());
			goal.setError(levels.isPowerReserveError());
			techSL.light(levels.isPivotPointTempLevel(), Color.GREEN);
		});

		autoUpdate.addUpdater(() -> exchangeBox.setSelectedItem(autoUpdate.getExchange()));

		autoUpdate.addUpdater(() -> {
			levels.setBestPrice(autoUpdate.getBestPrice());
			level.setValue(autoUpdate.getSpread());
			levels.setHighDay(autoUpdate.getHigh());
			levels.setLowDay(autoUpdate.getLow());
			calculator.setSpread(autoUpdate.getSpread());
			levels.calculatePivotPoint(calculator.getPositionType());
			levels.calculatePowerReserve(calculator.getPositionType());
			calculator.estimate();
		});

		lock.addActionListener(e -> calculator.estimate());
		stopLimit.addActionListener(e -> {
			calculator.reset();
			levels.reset();
		});

		PositionPanel position = new PositionPanel();
		InfoPanel	 info     = new InfoPanel(15, 20, 5,20, 5, 5);

		MessagePanel messagePanel = new MessagePanel(20, 0);
		messagePanel.show("Welcome to Trade!");

		panel.add(exchangeBox);
		panel.add(tickerLookup);
		panel.add(positionBox);
		panel.add(estimationBox);
		panel.add(level);
		panel.add(goal);
		panel.add(techSL);
		panel.add(messagePanel);
		panel.add(orderPanel);
		panel.add(position);
		panel.add(info);

		position.addPosition("AAPL");
		position.addPosition("MSFT");

		this.add(panel, BorderLayout.NORTH);
	}
}
