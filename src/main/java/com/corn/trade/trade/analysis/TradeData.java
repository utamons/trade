package com.corn.trade.trade.analysis;

import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;

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

	private TradeData(PositionType positionType,
	                  EstimationType estimationType,
	                  Double powerReserve,
	                  Double price,
	                  Double level,
	                  Double techStopLoss,
	                  Double slippage,
	                  Double goal,
	                  Double luft) {
		this.positionType = positionType;
		this.estimationType = estimationType;
		this.powerReserve = powerReserve;
		this.price = price;
		this.level = level;
		this.techStopLoss = techStopLoss;
		this.slippage = slippage;
		this.goal = goal;
		this.luft = luft;
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

	public static final class Builder {
		private PositionType   positionType;
		private EstimationType estimationType;
		private Double         powerReserve;
		private Double         price;
		private Double         level;
		private Double         techStopLoss;
		private Double         slippage;
		private Double         goal;
		private Double         luft;

		private Builder() {
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
			return new TradeData(positionType,estimationType, powerReserve, price, level, techStopLoss, slippage, goal, luft);
		}
	}
}
