package com.corn.trade.trade;

import com.corn.trade.util.functional.Trigger;

import javax.swing.*;
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
	private final Double REALISTIC_POWER_RESERVE = 0.8;
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
	private Double  powerReserve;

	private boolean powerReserveError = false;
	private Double  level;

	private boolean levelError = false;
	private Double  atr;

	private boolean atrError = false;
	private Double  highDay;

	private boolean highDayError = false;
	private Double  lowDay;

	private boolean lowDayError = false;
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
	private Double  price;
	private boolean priceError      = false;
	private Double  minTake;
	private Double  maxTake;
	private boolean supportError    = false;
	private boolean resistanceError = false;

	public Calculator(Component frame) {
		this.frame = frame;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isSupportError() {
		return supportError;
	}

	public boolean isResistanceError() {
		return resistanceError;
	}

	public Double getMaxTake() {
		return maxTake;
	}

	public void setMaxTake(Double maxTake) {
		this.maxTake = maxTake;
	}

	public Double getMinTake() {
		return minTake;
	}

	public void setMinTake(Double minTake) {
		this.minTake = minTake;
	}

	private boolean isPriceError() {
		return priceError;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	private void announce() {
		triggers.forEach(Trigger::trigger);
	}

	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}

	public boolean isSpreadError() {
		return spreadError;
	}

	public boolean isPowerReserveError() {
		return powerReserveError;
	}

	public boolean isLevelError() {
		return levelError;
	}

	public boolean isAtrError() {
		return atrError;
	}

	public boolean isHighDayError() {
		return highDayError;
	}

	public boolean isLowDayError() {
		return lowDayError;
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
		return powerReserve;
	}

	public void setPowerReserve(Double powerReserve) {
		this.powerReserve = powerReserve;
	}

	public Double getLevel() {
		return level;
	}

	public void setLevel(Double level) {
		this.level = level;
	}

	public Double getAtr() {
		return atr;
	}

	public void setAtr(Double atr) {
		this.atr = atr;
	}

	public Double getHighDay() {
		return highDay;
	}

	public void setHighDay(Double highDay) {
		this.highDay = highDay;
	}

	public Double getLowDay() {
		return lowDay;
	}

	public void setLowDay(Double lowDay) {
		this.lowDay = lowDay;
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
		this.quantity = quantity.intValue();
	}

	public void calculatePowerReserve() {
		String error = validPowerReserve();
		if (error != null) {
			log("Invalid power reserve - atr: {}, lowDay: {}, highDay: {}, level: {}", atr, lowDay, highDay, level);
			showErrorDlg(frame, error);
			announce();
			return;
		}

		double techAtr = highDay - lowDay;
		double realAtr = Math.max(techAtr, atr * REALISTIC_POWER_RESERVE);

		if (positionType == PositionType.LONG) {
			powerReserve = realAtr - (level - lowDay);
		} else {
			powerReserve = realAtr - (highDay - level);
		}
		announce();
	}

	private String validPowerReserve() {
		atrError = false;
		lowDayError = false;
		highDayError = false;
		levelError = false;
		if (atr == null) {
			atrError = true;
			return "atr must be set\n ";
		}
		if (lowDay == null) {
			lowDayError = true;
			return "lowDay must be set\n ";
		}
		if (highDay == null) {
			highDayError = true;
			return "highDay must be set\n ";
		}
		if (level == null) {
			levelError = true;
			return "level must be set\n ";
		}
		if (atr <= 0) {
			atrError = true;
			return "atr must be greater than 0\n ";
		}
		if (lowDay <= 0) {
			lowDayError = true;
			return "lowDay must be greater than 0\n ";
		}
		if (highDay <= 0) {
			highDayError = true;
			return "highDay must be greater than 0\n ";
		}
		if (level <= 0) {
			levelError = true;
			return "level must be greater than 0\n ";
		}
		if (lowDay >= highDay) {
			lowDayError = true;
			return "lowDay must be less than highDay\n ";
		}
		if (level <= lowDay || level >= highDay) {
			levelError = true;
			return "level must be between lowDay and highDay\n ";
		}
		return null;
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
		levelError = false;
		spreadError = false;
		powerReserveError = false;
		quantityError = false;
		String error = null;
		if (level == null) {
			levelError = true;
			error = "Level must be set\n ";
		} else if (level <= 0) {
			levelError = true;
			error = "Level must be greater than 0\n ";
		}
		if (spread == null) {
			spreadError = true;
			error = "Spread must be set\n ";
		} else if (spread <= 0) {
			spreadError = true;
			error = "Spread must be greater than 0\n ";
		}
		if (powerReserve == null) {
			powerReserveError = true;
			error = "Power reserve must be set\n ";
		} else if (powerReserve <= 0) {
			powerReserveError = true;
			error = "Power reserve must be greater than 0\n ";
		}
		if (quantity != null && quantity <= 0) {
			quantityError = true;
			error = "Quantity must be greater than 0\n ";
		}
		if (error != null) {
			log("Invalid estimation - level: {}, spread: {}, powerReserve: {}, quantity: {}",
			    level,
			    spread,
			    powerReserve,
			    quantity);
			showErrorDlg(frame, error);
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
			double minStopLoss = isLong() ? level - Math.max(ORDER_LUFT, spread) : level + Math.max(ORDER_LUFT, spread);
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit < breakEven) || (isShort() && takeProfit > breakEven)) {
			log("Take profit is less than break even");
			showErrorDlg(frame,"Cannot fit to risk limits!");
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
			showErrorDlg(frame,"Cannot fit to risk limits!");
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
		orderStop = level;
		orderLimit = orderStop + (isLong() ? spread : -spread);
		takeProfit = isLong() ? level + powerReserve : level - powerReserve;
	}

	private boolean stopLossTooLow() {
		return (isLong() && stopLoss > level - Math.max(ORDER_LUFT, spread)) ||
		       (isShort() && stopLoss < level + Math.max(ORDER_LUFT, spread));
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / level);
	}

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
		atr = null;
		highDay = null;
		lowDay = null;
		spread = null;
		powerReserve = null;
		level = null;
		takeProfitError = false;
		stopLossError = false;
		quantityError = false;
		levelError = false;
		spreadError = false;
		powerReserveError = false;
		atrError = false;
		lowDayError = false;
		highDayError = false;
		announce();
	}
}
