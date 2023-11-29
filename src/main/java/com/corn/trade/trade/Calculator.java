package com.corn.trade.trade;

import com.corn.trade.util.Trigger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Calculator {

	private final List<Trigger> triggers = new ArrayList<>();
	private       PositionType  positionType;
	private EstimationType estimationType;
	private Double         spread;
	private Double         powerReserve;
	private Double         level;
	private Double         atr;
	private Double         highDay;
	private Double lowDay;
	private Double stopLoss;
	private Double takeProfit;
	private Double breakEven;
	private Double risk;
	private Double riskPercent;
	private Double riskRewardRatioPercent;
	private Double orderLimit;
	private Double orderStop;
	private int quantity;

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
}
