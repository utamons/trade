package com.corn.trade.trade;

import com.corn.trade.util.functional.Trigger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.log;
import static com.corn.trade.util.Util.showErrorDlg;
import static java.lang.Math.abs;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Calculator {
	private final Double MAX_VOLUME              = 5000.0;
	private final double MAX_RISK_PERCENT        = 0.5;
	private final double MAX_RISK_REWARD_RATIO   = 3.0;
	private final Double ORDER_LUFT              = 0.02;
	private final List<Trigger>  triggers   = new ArrayList<>();
	private final Component      frame;
	private       boolean        autoUpdate = false;
	private       PositionType   positionType;
	private       EstimationType estimationType;
	private       Double         outputExpected;
	private       Double         gain;
	private       Double         spread;

	private boolean spreadError = false;

	private Double  stopLoss;

	private boolean stopLossError = false;
	private Double  takeProfit;

	private boolean takeProfitError = false;
	private Double  breakEven;
	private Double  risk;
	private Double  riskPercent;
	private Double  riskRewardRatioPercent;
	private Double  orderLimit;
	private Double  orderStop;
	private Integer quantity;
	private boolean quantityError   = false;
	private Double  bestPrice;
	private final Levels levels;

	public Calculator(Component frame) {
		this.frame = frame;
		levels = new Levels();
	}

	public boolean isResistanceError() {
		return levels.isResistanceError();
	}

	public boolean isSupportError() {
		return levels.isSupportError();
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public Double getResistance() {
		return levels.getResistance();
	}

	public void setResistance(Double resistance) {
		levels.setResistance(resistance);
	}

	public Double getSupport() {
		return levels.getSupport();
	}

	public void setSupport(Double support) {
		levels.setSupport(support);
	}

	public Double getBestPrice() {
		return bestPrice;
	}

	public void setBestPrice(Double bestPrice) {
		this.bestPrice = bestPrice;
	}

	private void announce() {
		triggers.forEach(Trigger::trigger);
	}

	public void addUpdater(Trigger trigger) {
		triggers.add(trigger);
	}

	public boolean isSpreadError() {
		return spreadError;
	}

	public boolean isPowerReserveError() {
		return levels.isPowerReserveError();
	}

	public boolean isTempLevelError() {
		return levels.isTempLevelError();
	}

	public boolean isAtrError() {
		return levels.isAtrError();
	}

	public boolean isHighDayError() {
		return levels.isHighDayError();
	}

	public boolean isLowDayError() {
		return levels.isLowDayError();
	}

	public boolean isStopLossError() {
		return stopLossError;
	}

	public boolean isQuantityError() {
		return quantityError;
	}

	public boolean isTakeProfitError() {
		return takeProfitError;
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

	public Double getPowerReserve() {
		return levels.getPowerReserve();
	}

	public void setPowerReserve(Double powerReserve) {
		levels.setPowerReserve(powerReserve);
	}

	public Double getTempLevel() {
		return levels.getTempLevel();
	}

	public void setTempLevel(Double tempLevel) {
		levels.setTempLevel(tempLevel);
	}

	public Double getAtr() {
		return levels.getAtr();
	}

	public void setAtr(Double atr) {
		levels.setAtr(atr);
	}

	public Double getHighDay() {
		return levels.getHighDay();
	}

	public void setHighDay(Double highDay) {
		levels.setHighDay(highDay);
	}

	public Double getLowDay() {
		return levels.getLowDay();
	}

	public void setLowDay(Double lowDay) {
		levels.setLowDay(lowDay);
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

	public void calculatePowerReserve() {
		String error = levels.validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return;
		}
		levels.calculatePowerReserve(positionType);
		announce();
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

		while (profit < openCommission + closeCommission + taxes) {
			priceClose = priceClose + increment;
			closeCommission = estimatedCommissionUSD(priceClose);
			profit = abs(priceClose - price) * quantity;
			taxes = getTaxes(profit);
		}

		return priceClose + (isLong() ? spread : -spread);
	}

	private double getTaxes(double sum) {
		return sum * 0.1; // ИПН
	}

	private boolean isValidEstimation() {
		takeProfitError = false;
		stopLossError = false;
		spreadError = false;
		quantityError = false;
		String error = levels.validate();
		if (error == null)
			error = levels.validatePowerReserve();
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
		if (error != null) {
			log("Invalid estimation - level: {}, spread: {}, powerReserve: {}, quantity: {}",
			    levels.getTempLevel(),
			    spread,
			    levels.getPowerReserve(),
			    quantity);
			showErrorDlg(frame, error, !autoUpdate);
			announce();
		}
		return error == null;
	}

	public void estimate() {
		if (!isValidEstimation())
			return;

		fillQuantity();
		fillOrder();

		do {
			breakEven = getBreakEven(orderLimit);
			if (areRiskLimitsFailed())
				return;
			double reward = getReward();
			risk = getRisk(reward);
			stopLoss = calculateStopLoss();

			// recalculate risk because stop loss might be corrected
			risk = recalculatedRisk();

			if (stopLossTooLow()) {
				decreaseTakeProfit();
			}

			fillTradeAndRiskFields(reward);

			if (riskPercent > MAX_RISK_PERCENT)
				quantity--;
		} while (riskPercent > MAX_RISK_PERCENT || stopLossTooLow());

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
		return isLong() ? breakEven - stopLoss + spread : stopLoss - breakEven + spread;
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
					isLong() ? levels.getTempLevel() - Math.max(ORDER_LUFT, spread) : levels.getTempLevel() + Math.max(ORDER_LUFT, spread);
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit < breakEven) || (isShort() && takeProfit > breakEven)) {
			log("Take profit is less than break even");
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			takeProfitError = true;
			stopLossError = stopLossTooLow();
			gain = 0.0;
			outputExpected = 0.0;
			risk = 0.0;
			riskPercent = 0.0;
			riskRewardRatioPercent = 0.0;
			announce();
			return true;
		}
		if (quantity <= 0) {
			log("Cannot fit to risk limits");
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			announce();
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
		orderStop = levels.getTempLevel();
		orderLimit = orderStop + (isLong() ? spread : -spread);
		takeProfit = isLong() ? levels.getTempLevel() + levels.getPowerReserve() : levels.getTempLevel() - levels.getPowerReserve();
	}

	private boolean stopLossTooLow() {
		return (isLong() && stopLoss > levels.getTempLevel() - Math.max(ORDER_LUFT, spread)) ||
		       (isShort() && stopLoss < levels.getTempLevel() + Math.max(ORDER_LUFT, spread));
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / levels.getTempLevel());
	}

	@SuppressWarnings("DuplicatedCode")
	public void reset() {
		levels.reset();
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
		takeProfitError = false;
		stopLossError = false;
		quantityError = false;
		spreadError = false;
		announce();
	}
}
