package com.corn.trade.ui.controller;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.BrokerFactory;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.DBException;
import com.corn.trade.model.ExtendedTradeContext;
import com.corn.trade.model.TradeContext;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
import com.corn.trade.ui.view.PositionPanel;
import com.corn.trade.ui.view.TradeView;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.corn.trade.BaseWindow.ORDER_LUFT;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.round;


public class TradeController implements TradeViewListener {
	public static final  int                         SEND_ORDER_DELAY    = 3000;
	private static final Logger                      log                 = LoggerFactory.getLogger(TradeController.class);
	private final        AssetService                assetService;
	private final        HashMap<String, TradeState> tradeStateMap       = new HashMap<>();
	private final        Timer                       lockButtonsTimer;
	private final        Map<String, Integer>        positionListenerIds = new HashMap<>();
	private              TradeView                   view;
	private              Double                      level;
	private              Double                      techStopLoss;
	private              Double                      goal;
	private              PositionType                positionType;
	private              EstimationType              estimationType;
	private              Exchange                    exchange;
	private              Broker                      currentBroker;
	private              Timer                       timeUpdater         = null;
	private              int                         tradeContextId      = 0;
	private              boolean                     locked              = false;
	private              boolean                     orderClean          = false;
	private              TradeData                   tradeData;
	private              PositionController          positionController;
	private              int                         pnlListenerId       = 0;

	public TradeController() {
		this.assetService = new AssetService();
		lockButtonsTimer = new Timer(SEND_ORDER_DELAY, e -> checkButtons());
		lockButtonsTimer.setRepeats(false);
	}

	@Override
	public void setView(TradeView view) {
		this.view = view;
	}

	@Override
	public void setPositionPanel(PositionPanel positionPanel) {
		this.positionController = new PositionController(positionPanel);
	}

	@Override
	public void onExchangeChange(String exchangeName) {
		try {
			exchange = assetService.getExchange(exchangeName);

			// Stop any previous time updater to prevent multiple timers running
			if (timeUpdater != null) {
				timeUpdater.stop();
			}

			startTimeUpdater(exchange);
		} catch (DBException e) {
			Util.showWarningDlg(view.asComponent(), e.getMessage());
			view.messagePanel().show(e.getMessage(), Color.RED);
		}
	}

	@Override
	public void onAssetChange(String assetName) {
		try {
			if (currentBroker != null) {
				/*
				   since we have one broker object per asset, we need a new broker object as the asset changes,
				   so we need to cancel updating the trade context from current broker and prepare widgets
				   for new trade context
				 */
				currentBroker.cancelTradeContext(tradeContextId);
			}
			saveTradeState();

			view.info().clear();
			view.goal().setValue(null);
			view.level().setValue(null);
			view.techSL().setValue(null);
			view.messagePanel().clear();
			level = null;
			techStopLoss = null;
			goal = null;
			view.techSL().setControlCheckBoxState(false);
			goalWarning(false);

			if (assetName == null || assetName.isBlank()) {
				view.exchangeBox().setEnabled(true);
				return;
			} else {
				view.exchangeBox().setEnabled(false);
			}

			currentBroker = BrokerFactory.getBroker(exchange.getBroker(), assetName, exchange.getName(), () -> {
				view.messagePanel().show(exchange.getBroker() + " disconnected.", Color.RED);
				view.assetLookup().clear();
				view.info().clear();
			});
			restoreTradeState();

			// Get the actual asset object from the asset name (and save it in the database if it doesn't exist)
			Asset asset = assetService.getAsset(assetName, view.exchangeBox().getSelectedItem(), currentBroker);
			String       brokerName   = asset.getExchange().getBroker();
			String       exchangeName = asset.getExchange().getName();
			List<String> assets       = assetService.getAssetNames();
			view.assetLookup().setItems(assets); // because we might have added a new asset

			// Change the exchange box to the actual exchange name from the broker
			if (!view.exchangeBox().getSelectedItem().equals(exchangeName)) {
				view.exchangeBox().setSelectedItem(exchangeName);
				view.assetLookup().setText(assetName); // because we cleared it on exchange change
			}

			view.messagePanel().show(brokerName + " is connected.");

			tradeContextId = currentBroker.requestTradeContext(this::tradeContextListener);
			if (pnlListenerId == 0) {
				pnlListenerId = currentBroker.addPnListener(pnl -> view.info().setPnl(pnl.realized()));
				currentBroker.requestPnLUpdates();
			}

		} catch (DBException | BrokerException e) {
			Util.showWarningDlg(view.asComponent(), e.getMessage());
			view.messagePanel().show(e.getMessage(), Color.RED);
			view.assetLookup().clear();
		}
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onPositionTypeChange(PositionType positionType) {
		this.positionType = positionType;
		view.techSL().setControlCheckBoxState(false);
		view.techSL().setValue(null);
		view.goal().setValue(null);
		goal = null;
		techStopLoss = null;
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onEstimationTypeChange(EstimationType estimationType) {
		this.estimationType = estimationType;
		if (estimationType == EstimationType.MIN_GOAL) {
			view.goal().setEditable(false);
			view.goal().setValue(null);
			goal = null;
		} else {
			view.goal().setEditable(true);
			goal = view.goal().getValue();
		}
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onLevelChange(Double level) {
		if (Objects.equals(this.level, level)) {
			return;
		}
		this.level = level;
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onTechStopLossChange(Double techStopLoss) {
		this.techStopLoss = techStopLoss;
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onGoalChange(Double goal) {
		this.goal = goal;
		if (estimationType != EstimationType.MIN_GOAL) {
			orderClean = false;
			checkButtons();
		}
	}

	private void startTimeUpdater(Exchange exchange) {
		ExchangeTime exchangeTime = new ExchangeTime(exchange);
		timeUpdater = new Timer(1000, e -> {
			if (exchangeTime.withinTradingHours()) {
				view.info().setTime(exchangeTime.getNowTime("HH:mm"), Color.GREEN.darker());
			} else {
				view.info().setTime(exchangeTime.getNowTime("HH:mm"));
			}
		});
		timeUpdater.setInitialDelay(0);
		timeUpdater.start();
	}

	private boolean workTime() {
		if (exchange == null) {
			return false;
		}
		ExchangeTime exchangeTime = new ExchangeTime(exchange);
		return exchangeTime.withinTradingHours() && exchangeTime.withinWeekDays();
	}

	private boolean canOpenPosition() {
		return !locked && orderClean && workTime() && !currentBroker.isOpenPosition();
	}

	private void goalWarning(boolean on) {
		if (on && !view.goal().isBlinking()) {
			view.goal().light(true, Color.ORANGE.darker(), true);
		} else if (!on && view.goal().isBlinking()) {
			view.goal().lightOff();
		}
	}

	private Double powerReserve() {
		if (level == null || goal == null) {
			return null;
		} else {
			return round(Math.abs(goal - level));
		}
	}

	private void tradeContextListener(TradeContext tradeContext) {
		TradeService         tradeService = new TradeService();
		ExtendedTradeContext ctx          = tradeService.getExtendedTradeContext(tradeContext, positionType);

		if (ctx == null) return;

		double high                 = ctx.getDayHigh();
		double low                  = ctx.getDayLow();
		double spread               = ctx.getSpread();
		double maxRange             = ctx.getMaxRange();
		double maxRangePassedForPos = ctx.getMaxRangePassedForPos();
		double maxRangeLeftForPos   = ctx.getMaxRangeLeftForPos();
		double price                = tradeContext.getPrice();
		double slippage             = ctx.getSlippage();

		view.info().setPrice(fmt(price));
		view.info().setMaxRangePassed(fmt(maxRangePassedForPos));
		view.info().setMaxRangeLeft(fmt(maxRangeLeftForPos < 0 ? 0 : maxRangeLeftForPos));
		view.info().setSpread(fmt(spread));

		if (level != null) {
			tradeData = TradeData.aTradeData()
			                     .withEstimationType(estimationType)
			                     .withPositionType(positionType)
			                     .withPowerReserve(powerReserve())
			                     .withPrice(price)
			                     .withLevel(level)
			                     .withLevel(level)
			                     .withTechStopLoss(techStopLoss)
			                     .withSlippage(slippage)
			                     .withGoal(goal)
			                     .withLuft(ORDER_LUFT)
			                     .build();
			try {
				tradeData = new TradeCalc(tradeData).calculate();
			} catch (Exception e) {
				view.messagePanel().show(e.getMessage(), Color.RED);
				view.trafficLight().setRed();
				goalWarning(false);
				orderClean = false;
				return;
			}

			view.info().setBe(fmt(tradeData.getBreakEven()));
			view.info().setRisk(fmt(tradeData.getRisk()) + " (" + fmt(tradeData.getRiskPercent()) + "%)");
			view.info().setRR(fmt(tradeData.getRiskRewardRatioPercent()));
			view.info().setSl(fmt(tradeData.getStopLoss()));
			view.info().setTp(fmt(tradeData.getTakeProfit()));
			view.info().setOut(fmt(tradeData.getOutputExpected()) + " (" + fmt(tradeData.getGain()) + "%)");

			if (estimationType == EstimationType.MIN_GOAL) {
				view.goal().setValue(tradeData.getGoal());
			}

			double goalFromHigh = high - tradeData.getGoal();
			double goalFromLow  = tradeData.getGoal() - low;
			double goalToPass   = positionType == PositionType.LONG ? goalFromLow : goalFromHigh;

			// todo this is the place for asking RiskManager for permission to trade
			if (tradeData.hasError()) {
				view.trafficLight().setRed();
				view.messagePanel().show(tradeData.getTradeError(), Color.RED.darker());
				view.info().setBe(null);
				view.info().setRisk(null);
				view.info().setRR(null);
				view.info().setSl(null);
				view.info().setTp(null);
				view.info().setOut(null);
				goalWarning(false);
				orderClean = false;
			} else if (goalToPass > maxRange) {
				goalWarning(true);
				view.trafficLight().setGreen();
				view.messagePanel().show("Goal is too far", Color.ORANGE.darker());
				orderClean = !tradeContext.isPositionOpen();
			} else {
				goalWarning(false);
				view.trafficLight().setGreen();
				view.messagePanel().show("Good to go", Color.GREEN.darker());
				orderClean = !tradeContext.isPositionOpen();
			}

			checkButtons();
		}
	}

	private void saveTradeState() {
		if (currentBroker == null) {
			return;
		}
		TradeState tradeState = new TradeState(positionType, estimationType, level, techStopLoss, goal);
		tradeStateMap.put(currentBroker.getName(), tradeState);
	}

	private void restoreTradeState() {
		TradeState tradeState = tradeStateMap.get(currentBroker.getName());
		if (tradeState == null) {
			return;
		}

		view.positionTypeBox().setSelectedItem(tradeState.positionType.toString());
		view.estimationBox().setSelectedItem(tradeState.estimationType.toString());
		view.level().setValue(tradeState.level);
		view.techSL().setValue(tradeState.techStopLoss);
		view.goal().setValue(tradeState.goal);
		level = tradeState.level;
		techStopLoss = tradeState.techStopLoss;
		goal = tradeState.goal;
		positionType = tradeState.positionType;
		estimationType = tradeState.estimationType;

		view.techSL().setControlCheckBoxState(tradeState.techStopLoss != null);
	}

	@Override
	public void onLock() {
		locked = !locked;
		view.lockButton().setText(locked ? "Unlock" : "Lock");
		view.stopLimitButton().setEnabled(!locked && orderClean);
		view.limitButton().setEnabled(!locked && orderClean);
	}

	private void checkButtons() {
		view.stopLimitButton().setEnabled(canOpenPosition());
		view.limitButton().setEnabled(canOpenPosition());
	}

	@Override
	public void onLimit() {
		if (tradeData == null) {
			return;
		}
		pauseButtons();
		TradeData order = tradeData.copy().withOrderStop(null).build();
		try {
			openPosition(order);
			view.messagePanel().show("Limit order sent", Color.GREEN.darker());
		} catch (BrokerException e) {
			view.messagePanel().show(e.getMessage(), Color.RED);
		}
	}

	private void openPosition(TradeData order) {
		int id = currentBroker.addPositionListener((position) -> {
			if (position.getQuantity() == 0) {
				removePositionListener(position.getSymbol());
			}
			positionController.updatePosition(currentBroker, order, position);
		});
		log.debug("Position listener added with id {}", id);
		positionListenerIds.put(currentBroker.getAssetName(), id);

		currentBroker.openPosition(order);
	}

	private void removePositionListener(String assetName) {
		if (positionListenerIds.containsKey(assetName)) {
			log.debug("Removing position listener with id {}", positionListenerIds.get(assetName));
			currentBroker.removePositionListener(positionListenerIds.get(assetName));
		}
	}

	@Override
	public void onStopLimit() {
		if (tradeData == null) {
			return;
		}
		pauseButtons();
		try {
			openPosition(tradeData);
			view.messagePanel().show("Stop-Limit order sent", Color.GREEN.darker());
		} catch (BrokerException e) {
			view.messagePanel().show(e.getMessage(), Color.RED);
		}
	}

	private void pauseButtons() {
		view.stopLimitButton().setEnabled(false);
		view.limitButton().setEnabled(false);
		lockButtonsTimer.start();
	}


	private static class TradeState {
		final PositionType   positionType;
		final EstimationType estimationType;
		final Double         level;
		final Double techStopLoss;
		final Double goal;

		TradeState(PositionType positionType,
		           EstimationType estimationType,
		           Double level,
		           Double techStopLoss,
		           Double goal) {
			this.positionType = positionType;
			this.estimationType = estimationType;
			this.level = level;
			this.techStopLoss = techStopLoss;
			this.goal = goal;
		}
	}
}
