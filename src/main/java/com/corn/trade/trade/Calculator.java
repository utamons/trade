package com.corn.trade.trade;

import com.corn.trade.util.functional.Trigger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.log;
import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class Calculator {

	private final List<Trigger>  triggers = new ArrayList<>();
	private       PositionType   positionType;
	private       EstimationType estimationType;
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
	private       int            quantity;

	private final Component frame;

	public Calculator(Component frame) {
		this.frame = frame;
	}

	private void announce() {
		triggers.forEach(Trigger::trigger);
	}

	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
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

	public int getQuantity() {
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
		double realAtr = techAtr > atr ? techAtr : atr;

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
}
