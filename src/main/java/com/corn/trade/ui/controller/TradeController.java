/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.corn.trade.risk.RiskManager;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
import com.corn.trade.ui.view.PositionPanel;
import com.corn.trade.ui.view.TradeView;
import com.corn.trade.util.ExchangeTime;
import com.corn.trade.util.Util;
import liquibase.exception.DatabaseException;
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
	public               double                      DEFAULT_STOP_LOSS = 0.05;
	public static final  int                         SEND_ORDER_DELAY    = 3000;
	private static final Logger                      log                 = LoggerFactory.getLogger(TradeController.class);
	private final        AssetService                assetService;
	private final        HashMap<String, TradeState> tradeStateMap       = new HashMap<>();
	private final        Timer                         lockButtonsTimer;
	private final        Map<String, PositionListener> positionListeners = new HashMap<>();
	private final        RiskManager                   riskManager;
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

	public TradeController() throws DatabaseException {
		this.assetService = new AssetService();
		lockButtonsTimer = new Timer(SEND_ORDER_DELAY, e -> checkButtons());
		lockButtonsTimer.setRepeats(false);
		riskManager = new RiskManager();
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
			view.messagePanel().error(e.getMessage());
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
			view.techSL().setDefaultValue(null);
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
				view.messagePanel().error(exchange.getBroker() + " disconnected.");
				view.assetLookup().clear();
				view.info().clear();
			});
			restoreTradeState();

			// Get the actual asset object from the asset name (and save it in the database if it doesn't exist)
			Asset asset =
					assetService.getAsset(assetName, view.exchangeBox().getSelectedItem(), currentBroker);
			String       brokerName   = asset.getExchange().getBroker();
			String       exchangeName = asset.getExchange().getName();
			List<String> assets       = assetService.getAssetNames();
			view.assetLookup().setItems(assets); // because we might have added a new asset

			// Change the exchange box to the actual exchange name from the broker
			if (!view.exchangeBox().getSelectedItem().equals(exchangeName)) {
				view.exchangeBox().setSelectedItem(exchangeName);
				view.assetLookup().setText(assetName); // because we cleared it on exchange change
			}

			view.messagePanel().info(brokerName + " is connected.");

			tradeContextId = currentBroker.requestTradeContext(this::tradeContextListener);
			if (pnlListenerId == 0) {
				pnlListenerId = currentBroker.addPnListener(pnl -> {
					view.info().setPnl(pnl.realized());
					try {
						riskManager.updatePnL(pnl);
					} catch (DatabaseException e) {
						view.messagePanel().error(e.getMessage());
					}
				});
				currentBroker.requestPnLUpdates();
			}

		} catch (DBException | BrokerException e) {
			Util.showWarningDlg(view.asComponent(), e.getMessage());
			view.messagePanel().error(e.getMessage());
			view.assetLookup().clear();
		}
		orderClean = false;
		checkButtons();
	}

	@Override
	public void onPositionTypeChange(PositionType positionType) {
		this.positionType = positionType;
		setDefaultStopLoss(level, true);
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
		goalWarning(false);
		checkButtons();
	}

	@Override
	public void onLevelChange(Double level) {
		if (Objects.equals(this.level, level)) {
			return;
		}
		this.level = level;
		setDefaultStopLoss(level,true);
		orderClean = false;
		goalWarning(false);
		checkButtons();
	}

	private void setDefaultStopLoss(Double level, boolean activate) {
		if (level != null) {
			double stopLoss = positionType.equals(PositionType.LONG) ? level - DEFAULT_STOP_LOSS : level + DEFAULT_STOP_LOSS;
			view.techSL().setDefaultValue(stopLoss);
			if (activate) {
				view.techSL().setValue(stopLoss);
				view.techSL().setControlCheckBoxState(true);
				this.techStopLoss = stopLoss;
			}
		}
	}

	@Override
	public void onTechStopLossChange(Double techStopLoss) {
		this.techStopLoss = techStopLoss;
		orderClean = false;
		goalWarning(false);
		checkButtons();
	}

	@Override
	public void onGoalChange(Double goal) {
		this.goal = goal;
		if (estimationType != EstimationType.MIN_GOAL) {
			orderClean = false;
			goalWarning(false);
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
		if (exchange.getName().equals("TEST")) {
			return true;
		}
		ExchangeTime exchangeTime = new ExchangeTime(exchange);
		return exchangeTime.withinTradingHours() && exchangeTime.withinWeekDays();
	}

	private boolean canOpenPosition() {
		if (currentBroker == null) {
			return false;
		}
		return !locked && orderClean && workTime() && !currentBroker.isOpenPosition() && riskManager.canTrade();
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
		DEFAULT_STOP_LOSS           = round(ctx.getDefaultStopLoss());

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
				view.messagePanel().error(e.getMessage());
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

			if (!riskManager.canTrade()) {
				view.trafficLight().setRed();
				view.messagePanel().error(riskManager.getRiskError());
			}
			if (tradeData.hasError()) {
				view.trafficLight().setRed();
				view.messagePanel().error(tradeData.getTradeError());
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
				view.messagePanel().warning("Goal is too far");
				orderClean = true;
			} else {
				goalWarning(false);
				view.trafficLight().setGreen();
				view.messagePanel().info("Good to go");
				orderClean = true;
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
		setDefaultStopLoss(level, false);
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
		log.info("Limit order requested");
		if (tradeData == null) {
			log.debug("Trade data is null");
			return;
		}
		if (tradeData.tooFar()) {
			view.messagePanel().error("Limit price is too far from the current price");
			return;
		}
		if (tradeData.isStopLossHit()) {
			view.messagePanel().error("Stop loss is hit");
			return;
		}
		pauseButtons();
		TradeData order = tradeData.copy().withOrderStop(null).build();
		try {
			openPosition(order, currentBroker.getName());
			view.messagePanel().success("Limit order sent");
		} catch (BrokerException e) {
			view.messagePanel().error(e.getMessage());
		}
	}

	private void openPosition(TradeData tradeData, String brokerName) {
		int id = currentBroker.addPositionListener((position) -> {
			if (!Objects.equals(positionListeners.get(position.getSymbol()).id, position.getListenerId())) {
				log.warn("Listener id mismatch for symbol: {} expected: {} actual: {}", position.getSymbol(),
				         positionListeners.get(position.getSymbol()).id(), position.getListenerId());
				return;
			}
			if (position.getQuantity() == 0) {
				removePositionListener(position.getSymbol());
			}
			positionController.updatePosition(brokerName, tradeData, position);
		});
		log.debug("Position listener added with id {}", id);
		positionListeners.put(currentBroker.getAssetName(), new PositionListener(id, currentBroker));

		currentBroker.openPosition(tradeData, riskManager);
	}

	private void removePositionListener(String assetName) {
		if (positionListeners.containsKey(assetName)) {
			PositionListener positionListener = positionListeners.get(assetName);
			log.debug("Removing position listener with id {}", positionListener.id);
			positionListener.broker.removePositionListener(positionListener.id);
			positionListeners.remove(assetName);
		} else {
			log.warn("No position listener found for asset {}", assetName);
		}
	}

	@Override
	public void onStopLimit() {
		log.info("Stop-Limit order requested");
		if (tradeData == null) {
			log.debug("Trade data is null");
			return;
		}
		pauseButtons();
		try {
			openPosition(tradeData, currentBroker.getName());
			view.messagePanel().success("Stop-Limit order sent");
		} catch (BrokerException e) {
			view.messagePanel().error(e.getMessage());
		}
	}

	@Override
	public void checkRisk() {
		if (!riskManager.canTrade()) {
			view.trafficLight().setRed();
			view.messagePanel().error(riskManager.getRiskError());
		}
	}

	private void pauseButtons() {
		view.stopLimitButton().setEnabled(false);
		view.limitButton().setEnabled(false);
		lockButtonsTimer.start();
	}


	private record TradeState(PositionType positionType,
	                          EstimationType estimationType,
	                          Double level,
	                          Double techStopLoss,
	                          Double goal) {}

	private record PositionListener(int id, Broker broker) {
	}
}

