package com.corn.trade.trade;

import com.corn.trade.util.functional.Trigger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.log;
import static com.corn.trade.util.Util.round;
import static java.lang.Math.abs;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Calculator {
	private final Double MAX_VOLUME                    = 5000.0;
	private final double MAX_RISK_PERCENT              = 0.5;
	private final double MAX_RISK_REWARD_RATIO_PERCENT = 33.0;
	private final Double REALISTIC_POWER_RESERVE       = 0.8;
	private final Double ORDER_LUFT                    = 0.02;

	private final List<Trigger>  triggers = new ArrayList<>();
	private final Component      frame;
	private       PositionType   positionType;
	private       EstimationType estimationType;
	private       Double         outputExpected;
	private       Double         gain;
	private       Double         spread;
	private       Double         powerReserve;
	private       Double         level;
	private       Double         atr;
	private       Double         highDay;
	private       Double         lowDay;
	private       Double         stopLoss;
	private       Double         takeProfit;
	private       Double         breakEven;
	private       Double         risk;
	private       Double         riskPercent;
	private       Double         riskRewardRatioPercent;
	private       Double         orderLimit;
	private       Double         orderStop;
	private       Integer        quantity;

	public Calculator(Component frame) {
		this.frame = frame;
	}

	private void announce() {
		triggers.forEach(Trigger::trigger);
	}

	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
	}

	public Double getOutputExpected() {
		return round(outputExpected);
	}

	public Double getGain() {
		return round(gain);
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
		System.out.println("Estimation type set to " + estimationType);
		this.estimationType = estimationType;
	}

	public Double getSpread() {
		return spread;
	}

	public void setSpread(Double spread) {
		this.spread = spread;
	}

	public Double getPowerReserve() {
		return round(powerReserve);
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
		return round(stopLoss);
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getTakeProfit() {
		return round(takeProfit);
	}

	public void setTakeProfit(Double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public Double getBreakEven() {
		return round(breakEven);
	}

	public Double getRisk() {
		return round(risk);
	}

	public Double getRiskPercent() {
		return round(riskPercent);
	}

	public Double getRiskRewardRatioPercent() {
		return round(riskRewardRatioPercent);
	}

	public Double getOrderLimit() {
		return round(orderLimit);
	}

	public Double getOrderStop() {
		return round(orderStop);
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
			JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		double techAtr = highDay - lowDay;
		double realAtr = Math.max(techAtr, atr * REALISTIC_POWER_RESERVE);

		if (positionType == PositionType.LONG) {
			powerReserve = round(realAtr - (level - lowDay));
		} else {
			powerReserve = round(realAtr - (highDay - level));
		}
		announce();
	}

	private String validPowerReserve() {
		if (atr == null || lowDay == null || highDay == null || level == null) {
			return "atr, lowDay, highDay and level must be set\n ";
		}
		if (atr <= 0) {
			return "atr must be greater than 0\n ";
		}
		if (lowDay <= 0) {
			return "lowDay must be greater than 0\n ";
		}
		if (highDay <= 0) {
			return "highDay must be greater than 0\n ";
		}
		if (level <= 0) {
			return "level must be greater than 0\n ";
		}
		if (lowDay >= highDay) {
			return "lowDay must be less than highDay\n ";
		}
		if (level <= lowDay || level >= highDay) {
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

	private String validEstimation() {
		if (level == null) {
			return "Level must be set\n ";
		}
		if (spread == null) {
			return "Spread must be set\n ";
		}
		if (powerReserve == null) {
			return "Power reserve must be set\n ";
		}
		if (powerReserve <= 0) {
			return "Power reserve must be greater than 0\n ";
		}
		if (level <= 0) {
			return "Level must be greater than 0\n ";
		}
		if (spread <= 0) {
			return "Spread must be greater than 0\n ";
		}
		return null;
	}

	public void estimate() {
		String error = validEstimation();
		if (error != null) {
			log("Invalid estimation - level: {}, spread: {}, powerReserve: {}", level, spread, powerReserve);
			JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (quantity == null || estimationType == EstimationType.MAX_GAIN)
			quantity = maxQuantity();
		orderStop = level + (isLong() ? ORDER_LUFT : -ORDER_LUFT);
		orderLimit = orderStop + (isLong() ? spread : -spread);
		do {
			breakEven = getBreakEven(orderLimit);

			takeProfit = isLong() ? level + powerReserve : level - powerReserve;
			double reward = abs(takeProfit - breakEven);
			risk = reward / 3;

			/*
			 * The stop loss is corrected by the spread because the stop loss order might be executed by the worst price
			 * and this value should be a base for further calculations.
			 */
			stopLoss = isLong() ? breakEven - risk + spread : breakEven + risk - spread;

			riskRewardRatioPercent = risk / reward * 100;
			outputExpected = reward * quantity;
			gain = outputExpected / (orderLimit * quantity) * 100;
			risk = risk * quantity;
			riskPercent = risk / MAX_VOLUME * 100;
			if ((isLong() && stopLoss == level - ORDER_LUFT) || (!isLong() && stopLoss == level + ORDER_LUFT))
				break; // todo fix this
			if (riskPercent > MAX_RISK_PERCENT)
				quantity--;
		} while (riskPercent > MAX_RISK_PERCENT);

		announce();
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / level);
	}
}
