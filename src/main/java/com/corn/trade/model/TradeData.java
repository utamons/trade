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
package com.corn.trade.model;

import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;

@SuppressWarnings("unused")
public class TradeData {
	private final PositionType   positionType;
	private final EstimationType estimationType;
	private final Double         powerReserve;
	private final Double         price;
	private final Double         level;
	private final Double         techStopLoss;
	private final Double         slippage;
	private final Double         goal;
	private final Double         luft;
	private final long            quantity;
	private final Double         orderLimit;
	private final Double         orderStop;
	private final Double         stopLoss;
	private final Double         takeProfit;
	private final Double         breakEven;
	private final Double         risk;
	private final Double         outputExpected;
	private final Double         riskPercent;
	private final Double         gain;
	private final String         tradeError;
	private final Double         riskRewardRatioPercent;

	private TradeData(Builder builder) {
		this.positionType = builder.positionType;
		this.estimationType = builder.estimationType;
		this.powerReserve = builder.powerReserve;
		this.price = builder.price;
		this.level = builder.level;
		this.techStopLoss = builder.techStopLoss;
		this.slippage = builder.slippage;
		this.goal = builder.goal;
		this.luft = builder.luft;
		this.quantity = builder.quantity;
		this.orderLimit = builder.orderLimit;
		this.orderStop = builder.orderStop;
		this.stopLoss = builder.stopLoss;
		this.takeProfit = builder.takeProfit;
		this.breakEven = builder.breakEven;
		this.risk = builder.risk;
		this.outputExpected = builder.outputExpected;
		this.riskPercent = builder.riskPercent;
		this.gain = builder.gain;
		this.tradeError = builder.tradeError;
		this.riskRewardRatioPercent = builder.riskRewardRatioPercent;
	}

	public static Builder aTradeData() {
		return new Builder();
	}

	public EstimationType getEstimationType() {
		return estimationType;
	}

	public PositionType getPositionType() {
		return positionType;
	}

	public Double getPowerReserve() {
		return powerReserve;
	}

	public Double getPrice() {
		return price;
	}

	public Double getLevel() {
		return level;
	}

	public Double getTechStopLoss() {
		return techStopLoss;
	}

	public Double getSlippage() {
		return slippage;
	}

	public Double getGoal() {
		return goal;
	}

	public Double getLuft() {
		return luft;
	}

	public long getQuantity() {
		return quantity;
	}

	public Double getOrderLimit() {
		return orderLimit;
	}

	public Double getOrderStop() {
		return orderStop;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public Double getBreakEven() {
		return breakEven;
	}

	public Double getRisk() {
		return risk;
	}

	public Double getOutputExpected() {
		return outputExpected;
	}

	public Double getRiskPercent() {
		return riskPercent;
	}

	public Double getGain() {
		return gain;
	}

	public String getTradeError() {
		return tradeError;
	}

	public boolean hasError() {
		return tradeError != null;
	}

	public Double getRiskRewardRatioPercent() {
		return riskRewardRatioPercent;
	}

	public Builder copy() {
		return new Builder()
				.withPositionType(this.positionType)
				.withEstimationType(this.estimationType)
				.withPowerReserve(this.powerReserve)
				.withPrice(this.price)
				.withLevel(this.level)
				.withTechStopLoss(this.techStopLoss)
				.withSlippage(this.slippage)
				.withGoal(this.goal)
				.withLuft(this.luft)
				.withPositionType(this.positionType)
				.withEstimationType(this.estimationType)
				.withPowerReserve(this.powerReserve)
				.withLuft(this.luft)
				.withQuantity(this.quantity)
				.withOrderLimit(this.orderLimit)
				.withGain(this.gain)
				.withTradeError(this.tradeError)
				.withRiskPercent(this.riskPercent)
				.withOutputExpected(this.outputExpected)
				.withRisk(this.risk)
				.withBreakEven(this.breakEven)
				.withTakeProfit(this.takeProfit)
				.withStopLoss(this.stopLoss)
				.withRiskRewardRatioPercent(this.riskRewardRatioPercent)
				.withOrderStop(this.orderStop);
	}

	public String toSourceParams() {
		return String.format("""
				                     %s, %s,
				                     Lvl: %.2f, Goal: %.2f, Pwr: %.2f, Price: %.2f, Slpg: %.2f""",
		                     positionType, estimationType,
		                     level, goal, powerReserve, price, slippage);
	}

	@Override
	public String toString() {
		return String.format("""
				                     %s, %s,
				                     Lvl: %.2f, Goal: %.2f(%.2f%%), Pwr: %.2f, Price: %.2f, Slpg: %.2f
				                     				                    \s
				                     Params: Qtt: %d, SL: %.2f, BE: %.2f, TP: %.2f., Stop: %.2f, Limit: %.2f
				                     Out: %.2f, %.2f%%
				                     R/R - %.2f%%, Risk - %.2f, %.2f%%""",
		                     positionType, estimationType,
		                     level, goal, (powerReserve/level*100), powerReserve, price, slippage,
		                     quantity, stopLoss, breakEven, takeProfit, orderStop, orderLimit,
		                     outputExpected, gain, riskRewardRatioPercent, risk, riskPercent);
	}

	public boolean tooFar() {
		if (price == null || orderLimit == null) {
			return false; // Or handle it as an error case
		}

		double difference = Math.abs(price - orderLimit);
		double threshold = price * 0.01;
		return difference > threshold;
	}

	public boolean isStopLossHit() {
		double effectiveStopLoss = getEffectiveStopLoss();
		return (positionType == PositionType.LONG && price <= effectiveStopLoss) ||
		    (positionType == PositionType.SHORT && price >= effectiveStopLoss);
	}

	private double getEffectiveStopLoss() {
		return techStopLoss != null ? techStopLoss : stopLoss;
	}

	public static class Builder {
		private PositionType   positionType;
		private EstimationType estimationType;
		private Double         powerReserve;
		private Double         price;
		private Double         level;
		private Double         techStopLoss;
		private Double         slippage;
		private Double         goal;
		private Double         luft;
		private long            quantity;
		private Double         orderLimit;
		private Double         orderStop;
		private Double         stopLoss;
		private Double         takeProfit;
		private Double         breakEven;
		private Double         risk;
		private Double         outputExpected;
		private Double         riskPercent;
		private Double         gain;
		private String         tradeError;
		private double         riskRewardRatioPercent;


		private Builder() {
		}

		public Builder withRiskRewardRatioPercent(double riskRewardRatioPercent) {
			this.riskRewardRatioPercent = riskRewardRatioPercent;
			return this;
		}

		public Builder withQuantity(long quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder withOrderLimit(Double orderLimit) {
			this.orderLimit = orderLimit;
			return this;
		}

		public Builder withTradeError(String tradeError) {
			this.tradeError = tradeError;
			return this;
		}

		public Builder withGain(Double gain) {
			this.gain = gain;
			return this;
		}

		public Builder withRiskPercent(Double riskPercent) {
			this.riskPercent = riskPercent;
			return this;
		}

		public Builder withOutputExpected(Double outputExpected) {
			this.outputExpected = outputExpected;
			return this;
		}

		public Builder withRisk(Double risk) {
			this.risk = risk;
			return this;
		}

		public Builder withBreakEven(Double breakEven) {
			this.breakEven = breakEven;
			return this;
		}

		public Builder withTakeProfit(Double takeProfit) {
			this.takeProfit = takeProfit;
			return this;
		}

		public Builder withStopLoss(Double stopLoss) {
			this.stopLoss = stopLoss;
			return this;
		}

		public Builder withOrderStop(Double orderStop) {
			this.orderStop = orderStop;
			return this;
		}

		public Builder withEstimationType(EstimationType estimationType) {
			this.estimationType = estimationType;
			return this;
		}

		public Builder withPositionType(PositionType positionType) {
			this.positionType = positionType;
			return this;
		}

		public Builder withPowerReserve(Double powerReserve) {
			this.powerReserve = powerReserve;
			return this;
		}

		public Builder withPrice(Double price) {
			this.price = price;
			return this;
		}

		public Builder withLevel(Double level) {
			this.level = level;
			return this;
		}

		public Builder withTechStopLoss(Double techStopLoss) {
			this.techStopLoss = techStopLoss;
			return this;
		}

		public Builder withSlippage(Double slippage) {
			this.slippage = slippage;
			return this;
		}

		public Builder withGoal(Double goal) {
			this.goal = goal;
			return this;
		}

		public Builder withLuft(Double luft) {
			this.luft = luft;
			return this;
		}

		public TradeData build() {
			return new TradeData(this);
		}
	}

}
