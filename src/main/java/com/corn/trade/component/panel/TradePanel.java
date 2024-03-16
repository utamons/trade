package com.corn.trade.component.panel;

import com.corn.trade.broker.ibkr.AutoUpdate;
import com.corn.trade.component.*;
import com.corn.trade.component.position.PositionPanel;
import com.corn.trade.service.TickerService;
import com.corn.trade.trade.type.EstimationType;
import com.corn.trade.trade.type.PositionType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TradePanel extends BasePanel {

	public static final int TEXT_FIELD_SIZE = 10;

	public TradePanel(AutoUpdate autoUpdate, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super(autoUpdate, maxSize, minSize);

		TickerService tickerService = new TickerService();

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		final List<String> tickers = tickerService.getTickerNames();
		final List<String> exchanges = tickerService.getExchangeNames();

		LabeledLookup tickerLookup = new LabeledLookup("Ticker:", tickers, spacing, fieldHeight, autoUpdate::setTicker);
		autoUpdate.setTickerUpdateSuccessListener(tickerLookup::setSuccessStatus);


		LabeledComboBox exchangeBox =
				new LabeledComboBox("Exchange:", exchanges, spacing, fieldHeight, autoUpdate::setExchange);

		exchangeBox.setSelectedItem(autoUpdate.getExchange());

		LabeledComboBox positionBox = new LabeledComboBox("Position:",
		                                                  PositionType.getValues(),
		                                                  spacing,
		                                                  fieldHeight,
		                                                  (value) -> autoUpdate.setLong(PositionType.fromString(value) ==
		                                                                                PositionType.LONG));

		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:", EstimationType.getValues(), spacing,
		                                                    fieldHeight);


		LabeledDoubleField level = new LabeledDoubleField("Level:", TEXT_FIELD_SIZE, spacing, fieldHeight, false);

		LabeledDoubleField goal = new LabeledDoubleField("Goal:", TEXT_FIELD_SIZE, spacing, fieldHeight, false);

		LabeledDoubleField techSL = new LabeledDoubleField("Tech. SL:", TEXT_FIELD_SIZE, spacing, fieldHeight, true);


		TrafficLight trafficLight = new TrafficLight();

		RowPanel orderPanel = new RowPanel(20);

		JButton lock      = new JButton("Lock");
		JButton stopLimit = new JButton("Stop-Limit");
		JButton limit     = new JButton("Limit");

		orderPanel.add(lock);
		orderPanel.add(trafficLight);
		orderPanel.add(stopLimit);
		orderPanel.add(limit);

		autoUpdate.addUpdater(() -> exchangeBox.setSelectedItem(autoUpdate.getExchange()));

		PositionPanel position = new PositionPanel();
		InfoPanel info = InfoPanel.InfoPanelBuilder.anInfoPanel()
		                                           .withFontSize(15)
		                                           .withVPadding(20)
		                                           .withHPadding(5)
		                                           .withVgap(20)
		                                           .withHgap(5)
		                                           .withHSpacing(5)
		                                           .build();

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
