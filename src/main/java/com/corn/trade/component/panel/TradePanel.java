package com.corn.trade.component.panel;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.BrokerFactory;
import com.corn.trade.component.*;
import com.corn.trade.component.position.PositionPanel;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.service.AssetService;
import com.corn.trade.trade.analysis.data.TradeContext;
import com.corn.trade.trade.type.EstimationType;
import com.corn.trade.trade.type.PositionType;
import com.corn.trade.trade.type.TradeZone;
import com.corn.trade.util.Util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.corn.trade.util.Util.fmt;

public class TradePanel extends BasePanel {

	public static final int             TEXT_FIELD_SIZE = 10;
	private final       AssetService    assetService;
	private final       LabeledComboBox exchangeBox;
	private final       MessagePanel    messagePanel;
	private final       LabeledLookup   assetLookup;
	private final       InfoPanel       info;
	private final       LabeledComboBox positionBox;
	private             List<String>    tickers;
	private             Broker          broker;
	private             Exchange        exchange;
	private             int             tradeContextId  = 0;

	private Timer timeUpdater = null;

	public TradePanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) throws DBException {
		super(maxSize, minSize);

		assetService = new AssetService();

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		tickers = assetService.getTickerNames();

		final List<String> exchanges = assetService.getExchangeNames();
		this.setExchange(exchanges.get(0));

		assetLookup = new LabeledLookup("Ticker:", tickers, spacing, fieldHeight, this::setTicker);

		exchangeBox = new LabeledComboBox("Exchange:", exchanges, spacing, fieldHeight, this::setExchange);

		positionBox = new LabeledComboBox("Position:", PositionType.getValues(), spacing, fieldHeight);

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
		info = InfoPanel.InfoPanelBuilder.anInfoPanel()
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

	private void setExchange(String exchangeName) {
		try {
			exchange = assetService.getExchange(exchangeName);
			if (assetLookup != null) assetLookup.clear();
			String tradingHours = exchange.getTradingHours();
			String timeZone     = exchange.getTimeZone();

			// Stop any previous time updater to prevent multiple timers running
			if (timeUpdater != null) {
				timeUpdater.stop();
			}

			startTimeUpdater(tradingHours, timeZone);

		} catch (DBException e) {
			Util.showWarningDlg(this, e.getMessage());
			messagePanel.show(e.getMessage(), Color.RED);
		}
	}

	private void startTimeUpdater(String tradingHours, String timeZone) {
		// Timer task to update time every second
		timeUpdater = new Timer(1000, e -> {
			String[]  hoursParts   = tradingHours.split("-");
			LocalTime startTrading = LocalTime.parse(hoursParts[0], DateTimeFormatter.ofPattern("HH:mm"));
			LocalTime endTrading   = LocalTime.parse(hoursParts[1], DateTimeFormatter.ofPattern("HH:mm"));

			// Get current time in exchange's timezone
			ZonedDateTime nowInExchangeTimeZone = ZonedDateTime.now(ZoneId.of(timeZone));
			LocalTime     currentTime           = nowInExchangeTimeZone.toLocalTime();

			// Determine if current time is within trading hours
			boolean withinTradingHours = !currentTime.isBefore(startTrading) && !currentTime.isAfter(endTrading);

			if (withinTradingHours) {
				// Set working hours in green
				info.setTime(nowInExchangeTimeZone.format(DateTimeFormatter.ofPattern("HH:mm")), Color.GREEN);
			} else {
				// Set not working hours without color
				info.setTime(nowInExchangeTimeZone.format(DateTimeFormatter.ofPattern("HH:mm")));
			}
		});

		timeUpdater.setInitialDelay(0); // Start updating immediately
		timeUpdater.start(); // Start the timer
	}

	private PositionType positionType() {
		return PositionType.fromString(positionBox.getSelectedItem());
	}

	private void setTicker(String assetName) {
		if (assetName == null || assetName.isBlank()) {
			return;
		}
		try {
			if (broker != null) {
				broker.cancelTradeContext(tradeContextId);
				info.clear();
			}

			broker = BrokerFactory.getBroker(exchange.getBroker(), assetName, exchange.getName(), () -> {
				messagePanel.show(exchange.getBroker() + " disconnected.", Color.RED);
				assetLookup.clear();
			});
			Asset  asset        = assetService.getAsset(assetName, exchangeBox.getSelectedItem(), broker);
			String brokerName   = asset.getExchange().getBroker();
			String exchangeName = asset.getExchange().getName();
			tickers = assetService.getTickerNames();
			if (!exchangeBox.getSelectedItem().equals(exchangeName)) {
				exchangeBox.setSelectedItem(exchangeName);
				assetLookup.setText(assetName);
			}

			messagePanel.show(brokerName + " is connected.");

			tradeContextId = broker.requestTradeContext(this::populateTradeContext);

		} catch (DBException | BrokerException e) {
			Util.showWarningDlg(this, e.getMessage());
			messagePanel.show(e.getMessage(), Color.RED);
			assetLookup.clear();
		}
	}

	/*
	Double passed = positionType() == PositionType.LONG ? fromLow : fromHigh;
	double adrPassed = (passed / adr) * 100;
		double adrLeft   = 100 - adrPassed;

		info.setAdrPassed(fmt(adrPassed));
		info.setAdrLeft(fmt(adrLeft < 0 ? 0 : adrLeft));
	 */

	private void populateTradeContext(TradeContext tradeContext) {
		Double bestPrice = positionType() == PositionType.LONG ? tradeContext.getAsk() : tradeContext.getBid();
		Double high      = tradeContext.getDayHigh();
		Double low       = tradeContext.getDayLow();
		Double adr       = tradeContext.getAdr();

		if (bestPrice == null || high == null || low == null || adr == null) {
			return;
		}

		double range = high - low;

		// Calculate distances from high and low as a percentage of ADR
		double fromHighPercentage = (high - bestPrice) / range * 100;
		double fromLowPercentage  = (bestPrice - low) / range * 100;

		double adrUsed = range / adr * 100;
		if (adrUsed > 100) {
			adr = range;
		}

		double fromHigh        = high - bestPrice;
		double fromLow         = bestPrice - low;
		double passed          = positionType() == PositionType.LONG ? fromLow : fromHigh;
		double adrPassedForPos = (passed / adr) * 100;
		double adrLeftForPos   = 100 - adrPassedForPos;

		info.setPrice(fmt(bestPrice));
		info.setAdrPassed(fmt(adrPassedForPos));
		info.setAdrLeft(fmt(adrLeftForPos < 0 ? 0 : adrLeftForPos));

		// Determine the zone based on distances and ADR left
		TradeZone zone             = TradeZone.NEUTRAL; // Default to NEUTRAL
		boolean   isCloseToHighEnd = fromHighPercentage <= 30;
		boolean   isCloseToLowEnd  = fromLowPercentage <= 30;

		if (adrUsed >= 70) { // If more than 70% of ADR is used
			if (isCloseToLowEnd && !isCloseToHighEnd) {
				zone = TradeZone.LONG;
			} else if (isCloseToHighEnd && !isCloseToLowEnd) {
				zone = TradeZone.SHORT;
			}
		}

		info.setZone(zone.toString());
		info.setZoneRed((zone == TradeZone.SHORT && positionType() == PositionType.LONG) ||
		                (zone == TradeZone.LONG && positionType() == PositionType.SHORT));
	}

}
