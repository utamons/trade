package com.corn.trade.component.panel;

import com.corn.trade.broker.BrokerException;
import com.corn.trade.component.*;
import com.corn.trade.component.position.PositionPanel;
import com.corn.trade.entity.Asset;
import com.corn.trade.jpa.DBException;
import com.corn.trade.service.AssetService;
import com.corn.trade.trade.type.EstimationType;
import com.corn.trade.trade.type.PositionType;
import com.corn.trade.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TradePanel extends BasePanel {

	public static final int             TEXT_FIELD_SIZE = 10;
	private final       AssetService    assetService;
	private final       LabeledLookup   assetLookup;
	private final       LabeledComboBox exchangeBox;
	private final MessagePanel messagePanel;
	private             Asset           asset;

	public TradePanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super(maxSize, minSize);

		assetService = new AssetService();

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		final List<String> tickers   = assetService.getTickerNames();
		final List<String> exchanges = assetService.getExchangeNames();

		assetLookup = new LabeledLookup("Ticker:", tickers, spacing, fieldHeight, this::setTicker);

		exchangeBox = new LabeledComboBox("Exchange:", exchanges, spacing, fieldHeight);

		LabeledComboBox positionBox = new LabeledComboBox("Position:", PositionType.getValues(), spacing, fieldHeight);

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

		PositionPanel position = new PositionPanel();
		InfoPanel info = InfoPanel.InfoPanelBuilder.anInfoPanel()
		                                           .withFontSize(15)
		                                           .withVPadding(20)
		                                           .withHPadding(5)
		                                           .withVgap(20)
		                                           .withHgap(5)
		                                           .withHSpacing(5)
		                                           .build();

		messagePanel = new MessagePanel(20, 0);
		messagePanel.show("Welcome to Trade!");

		panel.add(exchangeBox);
		panel.add(assetLookup);
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

	private void setTicker(String assetName) {
		try {
			asset = assetService.getAsset(assetName, exchangeBox.getSelectedItem());
			exchangeBox.setSelectedItem(asset.getExchange().getName());
		} catch (DBException | BrokerException e) {
			Util.showWarningDlg(this, e.getMessage());
			messagePanel.show(e.getMessage(), Color.RED);
		}
	}
}
