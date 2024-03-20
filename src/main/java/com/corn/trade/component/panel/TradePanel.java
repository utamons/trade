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
import com.corn.trade.trade.analysis.TradeCalc;
import com.corn.trade.trade.analysis.data.TradeContext;
import com.corn.trade.trade.analysis.data.TradeData;
import com.corn.trade.trade.type.EstimationType;
import com.corn.trade.trade.type.PositionType;
import com.corn.trade.util.Util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.corn.trade.BaseWindow.ORDER_LUFT;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.round;

public class TradePanel extends BasePanel {

	public static final int                TEXT_FIELD_SIZE = 10;
	private final       AssetService       assetService;
	private final       LabeledComboBox    exchangeBox;
	private final       MessagePanel       messagePanel;
	private final       LabeledLookup      assetLookup;
	private final       InfoPanel          info;
	private final       LabeledComboBox    positionBox;
	private final       LabeledDoubleField goal;
	private final       LabeledDoubleField level;
	private final       LabeledDoubleField techSL;
	private final       LabeledComboBox    estimationBox;
	private final       TrafficLight       trafficLight;
	private             List<String>       tickers;
	private             Broker             broker;
	private             Exchange           exchange;
	private             int                tradeContextId  = 0;

	private Timer timeUpdater = null;

	public TradePanel(Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
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

		positionBox = new LabeledComboBox("Position:", PositionType.getValues(), spacing, fieldHeight, this::checkPosition);

		estimationBox = new LabeledComboBox("Estimation:", EstimationType.getValues(), spacing, fieldHeight, this::checkEstimationType);

		level = new LabeledDoubleField("Level:", TEXT_FIELD_SIZE, spacing, fieldHeight, false);

		goal = new LabeledDoubleField("Goal:", TEXT_FIELD_SIZE, spacing, fieldHeight, false);
		goal.setEditable(false);

		techSL = new LabeledDoubleField("Tech. SL:", TEXT_FIELD_SIZE, spacing, fieldHeight, true);

		trafficLight = new TrafficLight();

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

	private void checkPosition(String s) {
		techSL.setControlCheckBoxState(false);
		techSL.setValue(null);
		goal.setValue(null);
	}

	private void checkEstimationType(String estimationType) {
		if (EstimationType.fromString(estimationType) == EstimationType.MIN_GOAL) {
			goal.setEditable(false);
			goal.setValue(null);
		} else {
			goal.setEditable(true);
		}
	}

	private void goalWarning(boolean on) {
		if (on && !goal.isBlinking()) {
			goal.light(true, Color.ORANGE.darker(), true);
		} else if (!on && goal.isBlinking()) {
			goal.lightOff();
		}
	}

	private Double getLevel() {
		return level.isValidDouble() ? level.getValue() : null;
	}

	private Double getGoal() {
		return goal.isValidDouble() ? goal.getValue() : null;
	}

	private Double getTechSL() {
		return techSL.isValidDouble() ? techSL.getValue() : null;
	}

	private EstimationType estimationType() {
		return EstimationType.fromString(estimationBox.getSelectedItem());
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

			goal.setValue(null);
			level.setValue(null);
			techSL.setValue(null);
			techSL.setControlCheckBoxState(false);
			goalWarning(false);

		} catch (DBException | BrokerException e) {
			Util.showWarningDlg(this, e.getMessage());
			messagePanel.show(e.getMessage(), Color.RED);
			assetLookup.clear();
		}
	}

	private Double powerReserve() {
		if (getLevel() == null || getGoal() == null) {
			return null;
		} else {
			return round(Math.abs(getGoal() - getLevel()));
		}
	}

	private void populateTradeContext(TradeContext tradeContext) {
		Double ask = tradeContext.getAsk();
		Double bid = tradeContext.getBid();
		Double price = tradeContext.getPrice();
		Double high  = tradeContext.getDayHigh();
		Double low   = tradeContext.getDayLow();
		Double adr   = tradeContext.getAdr();

		if (price == null || high == null || low == null || adr == null) {
			return;
		}

		double slippage = positionType() == PositionType.LONG ? Math.abs(ask - price) : Math.abs(price - bid);

		double range = high - low;

		double spread = ask - bid;

		double adrUsed = range / adr * 100;
		if (adrUsed > 100) {
			adr = range;
		}

		double fromHigh        = high - price;
		double fromLow         = price - low;
		double passed          = positionType() == PositionType.LONG ? fromLow : fromHigh;
		double adrPassedForPos = (passed / adr) * 100;
		double adrLeftForPos   = 100 - adrPassedForPos;

		info.setPrice(fmt(price));
		info.setAdrPassed(fmt(adrPassedForPos));
		info.setAdrLeft(fmt(adrLeftForPos < 0 ? 0 : adrLeftForPos));

		if (getLevel() != null) {
			TradeData tradeData = TradeData.aTradeData()
			                               .withEstimationType(estimationType())
			                               .withPositionType(positionType())
			                               .withPowerReserve(powerReserve())
			                               .withPrice(price)
			                               .withLevel(getLevel())
			                               .withTechStopLoss(getTechSL())
			                               .withSlippage(slippage)
			                               .withGoal(getGoal())
			                               .withLuft(ORDER_LUFT)
			                               .build();
			try {
				tradeData = new TradeCalc(tradeData).calculate();
			} catch (Exception e) {
				messagePanel.show(e.getMessage(), Color.RED);
				trafficLight.setRed();
				goalWarning(false);
				return;
			}

			info.setBe(fmt(tradeData.getBreakEven()));
			info.setRisk(fmt(tradeData.getRisk()) + " (" + fmt(tradeData.getRiskPercent()) + ")");
			info.setRR(fmt(tradeData.getRiskRewardRatioPercent()));
			info.setSl(fmt(tradeData.getStopLoss()));
			info.setTp(fmt(tradeData.getTakeProfit()));
			info.setOut(fmt(tradeData.getOutputExpected()));
			info.setSpread(fmt(spread));

			if (estimationType() == EstimationType.MIN_GOAL) {
				goal.setValue(tradeData.getGoal());
			}

			double goalFromHigh        = high - tradeData.getGoal();
			double goalFromLow         = tradeData.getGoal() - low;
			double goalToPass          = positionType() == PositionType.LONG ? goalFromLow : goalFromHigh;

			if (tradeData.getTradeError() != null) {
				trafficLight.setRed();
				messagePanel.show(tradeData.getTradeError(), Color.RED.darker());
				goalWarning(false);
			} else if (goalToPass > adr) {
				goalWarning(true);
				trafficLight.setGreen();
				messagePanel.show("Goal is too far", Color.ORANGE.darker());
			} else {
				goalWarning(false);
				trafficLight.setGreen();
				messagePanel.show("Good to go", Color.GREEN.darker());
			}
		}
	}

}
