package com.corn.trade.trade;

import com.corn.trade.common.Notifier;
import com.corn.trade.util.Debug;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static com.corn.trade.App.*;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.showErrorDlg;
import static java.lang.Math.abs;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Calculator extends Notifier {
	public final  Debug          log                   = new Debug(LoggerFactory.getLogger(Calculator.class));
	private final Component      frame;
	private final Levels         levels;
	private       boolean        autoUpdate            = false;
	private       PositionType   positionType;
	private       EstimationType estimationType;
	private       Double         outputExpected;
	private       Double         gain;
	private       Double         spread;
	private       boolean        spreadError           = false;
	private       Double         stopLoss;
	private       Double         takeProfit;
	private       Double         breakEven;
	private       Double         risk;
	private       Double         riskPercent;
	private       Double         riskRewardRatioPercent;
	private       Double         orderLimit;
	private       Double         orderStop;
	private       Integer        quantity;
	private       boolean        quantityError         = false;

	private boolean tradeError = false;

	private boolean yellowLight = false;

	public Calculator(Component frame, Levels levels) {
		this.frame = frame;
		this.levels = levels;
	}

	public boolean isYellowLight() {
		return yellowLight;
	}

	public boolean isTradeError() {
		return tradeError;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isSpreadError() {
		return spreadError;
	}

	public boolean isQuantityError() {
		return quantityError;
	}

	public Double getOutputExpected() {
		return outputExpected;
	}

	public Double getGain() {
		return gain;
	}

	public PositionType getPositionType() {
		return positionType;
	}

	public void setPositionType(PositionType positionType) {
		this.positionType = positionType;
	}

	public EstimationType getEstimationType() {
		return estimationType;
	}

	public void setEstimationType(EstimationType estimationType) {
		this.estimationType = estimationType;
	}

	public Double getSpread() {
		return spread;
	}

	public void setSpread(Double spread) {
		this.spread = spread;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(Double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public Double getBreakEven() {
		return breakEven;
	}

	public Double getRisk() {
		return risk;
	}

	public Double getRiskPercent() {
		return riskPercent;
	}

	public Double getRiskRewardRatioPercent() {
		return riskRewardRatioPercent;
	}

	public Double getOrderLimit() {
		return orderLimit;
	}

	public Double getOrderStop() {
		return orderStop;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity == null ? null : quantity.intValue();
	}

	// For Interactive Brokers
	public double estimatedCommissionUSD(double price) {
		double max    = quantity * price / 100.0;
		double min    = quantity * 0.005;
		double amount = Math.min(max, min);
		return amount < 1 ? 1 : amount;
	}

	public double getBreakEven(double price) {
		double openCommission  = estimatedCommissionUSD(price);
		double priceClose      = price;
		double closeCommission = openCommission;
		double profit          = abs(priceClose - price) * quantity;
		double taxes           = getTaxes(profit);
		double increment       = isLong() ? 0.01 : -0.01;
		int    counter         = 0;

		while (profit < openCommission + closeCommission + taxes) {
			priceClose = priceClose + increment;
			closeCommission = estimatedCommissionUSD(priceClose);
			profit = abs(priceClose - price) * quantity;
			taxes = getTaxes(profit);
			counter++;
		}

		double range = abs(priceClose - price);
		double percent = range / price * 100;

		log.debug(2, "BE without spread: {}, range {}, {}%", fmt(priceClose), fmt(range), fmt(percent));

		return priceClose + (isLong() ? spread : -spread);
	}

	private double getTaxes(double sum) {
		return sum * 0.1; // ИПН
	}

	private boolean isValidEstimation() {
		quantityError = false;
		String error = levels.validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			log.error("Error: {}", error);
			announce();
			return false;
		}
		if (spread == null) {
			spreadError = true;
			error = "Spread must be set\n ";
		} else if (spread <= 0) {
			spreadError = true;
			error = "Spread must be greater than 0\n ";
		}
		if (quantity != null && quantity <= 0) {
			quantityError = true;
			error = "Quantity must be greater than 0\n ";
		}
		if (levels.getPivotPoint() == null) {
			levels.calculatePivotPoint(positionType);
		}
		if (levels.getPowerReserve() == null) {
			levels.calculatePowerReserve(positionType);
		}
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
		}

		return error == null;
	}

	public void checkYellowLight() {
		yellowLight = false;
		if (riskPercent == null || takeProfit == null || breakEven == null)
			return;
		if (MAX_RISK_PERCENT - riskPercent < 0.1) {
			log.debug(2,"YL: Risk percent {} is too close to max risk percent {}", fmt(riskPercent), MAX_RISK_PERCENT);
			yellowLight = true;
		}
		if (Math.abs(takeProfit - breakEven) < 0.05) {
			log.debug(2, "YL: Take profit {} is too close to break even {}", fmt(takeProfit), fmt(breakEven));
			yellowLight = true;
		}
		if (gain > 0 && gain < 0.5) {
			log.debug(2,"YL: Gain {} is less than 0.5%", fmt(gain));
			yellowLight = true;
		}
		if (!levels.isStopLossUnderLevels(stopLoss, positionType)) {
			log.debug(2,"YL: Stop loss {} is not under levels", fmt(stopLoss));
			yellowLight = true;
		}
	}

	public void estimate() {
		if (!isValidEstimation())
			return;

		levels.calculatePivotPoint(positionType);
		if (autoUpdate || levels.getPowerReserve() == null)
			levels.calculatePowerReserve(positionType);

		fillQuantity();
		fillOrder();

		tradeError = false;

		int counter = 0;
		do {
			counter++;
			breakEven = getBreakEven(orderLimit);
			double reward = getReward();
			risk = getRisk(reward);
			stopLoss = calculateStopLoss();
			// recalculate risk because stop loss might be corrected
			risk = recalculatedRisk();

			fillTradeAndRiskFields(reward);

			if (stopLossTooLow()) {
				break;
			}

			if (riskPercent > MAX_RISK_PERCENT)
				quantity--;
			log.debug(2,"Iteration {} - quantity: {}, take profit: {}, stop loss {}, risk percent: {}, risk reward ratio: {}",
			          counter,
			          quantity,
			          fmt(takeProfit),
			          fmt(stopLoss),
			          fmt(riskPercent),
			          fmt(riskRewardRatioPercent)
			);
		} while (
				riskPercent > MAX_RISK_PERCENT &&
				quantity > 0 &&
				((isLong() && takeProfit > breakEven) || (isShort() && takeProfit < breakEven))
		);

		if (areRiskLimitsFailed()) {
			tradeError = true;
		}
		checkYellowLight();

		announce();
	}

	private void fillTradeAndRiskFields(double reward) {
		riskRewardRatioPercent = risk / reward * 100;
		outputExpected = reward * quantity;
		gain = outputExpected / (orderLimit * quantity) * 100;
		risk = risk * quantity;
		riskPercent = risk / MAX_VOLUME * 100;
	}

	private double getRisk(double reward) {
		return reward / MAX_RISK_REWARD_RATIO;
	}

	private double recalculatedRisk() {
		return isLong() ? breakEven - stopLoss : stopLoss - breakEven;
	}

	private double getReward() {
		return isLong() ? takeProfit - breakEven : breakEven - takeProfit;
	}

	private void decreaseTakeProfit() {
		takeProfit = isLong() ? takeProfit - 0.01 : takeProfit + 0.01;
	}

	private double calculateStopLoss() {
		/*
		 * The stop loss is corrected by the spread because the stop loss order might be executed by the worst price
		 * and this value should be a base for further calculations.
		 */
		double stopLoss = isLong() ? breakEven - risk + spread : breakEven + risk - spread;

		if (estimationType == EstimationType.MIN_STOP_LOSS ||
		    estimationType == EstimationType.MAX_GAIN_MIN_STOP_LOSS) {
			double minStopLoss =
					isLong() ? levels.getPivotPoint() - Math.max(ORDER_LUFT, spread) : levels.getPivotPoint() +
					                                                                   Math.max(ORDER_LUFT, spread);
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit <= breakEven) || (isShort() && takeProfit >= breakEven)) {
			//log.debug("RL: Take profit {} is less than break even {}", fmt(takeProfit), fmt(breakEven));
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			return true;
		}
		if (quantity <= 0) {
			//log.debug("RL: Quantity {} is less than 0", quantity);
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			return true;
		}
		if (stopLossTooLow()) {
			//log.debug("RL: Stop loss {} is too low", fmt(stopLoss));
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			return true;
		}
		return false;
	}

	private boolean isShort() {
		return !isLong();
	}

	private void fillQuantity() {
		if (quantity == null ||
		    estimationType == EstimationType.MAX_GAIN_MAX_STOP_LOSS ||
		    estimationType == EstimationType.MAX_GAIN_MIN_STOP_LOSS)
			quantity = maxQuantity();
	}

	private void fillOrder() {
		orderStop = levels.getPivotPoint();
		orderLimit = orderStop + (isLong() ? spread : -spread);
		takeProfit = isLong() ? levels.getPivotPoint() + levels.getPowerReserve() : levels.getPivotPoint() -
		                                                                            levels.getPowerReserve();
	}

	private boolean stopLossTooLow() {
		return (isLong() && stopLoss > levels.getPivotPoint() - Math.max(ORDER_LUFT, spread)) ||
		       (isShort() && stopLoss < levels.getPivotPoint() + Math.max(ORDER_LUFT, spread));
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / levels.getPivotPoint());
	}

	@SuppressWarnings("DuplicatedCode")
	public void reset() {
		outputExpected = null;
		gain = null;
		stopLoss = null;
		takeProfit = null;
		breakEven = null;
		risk = null;
		riskPercent = null;
		riskRewardRatioPercent = null;
		orderLimit = null;
		orderStop = null;
		quantity = null;
		spread = null;
		quantityError = false;
		spreadError = false;
		announce();
	}
}
