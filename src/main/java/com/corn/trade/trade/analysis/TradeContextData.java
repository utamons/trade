package com.corn.trade.trade.analysis;

import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;

public class TradeContextData {
	private final Double         powerReserve;
	private final PositionType   positionType;
	private final EstimationType estimationType;
	private final Double         level;
	private final Double         goal;
	private final Double         slippage;
	private final Double         price;
	private final Double         techStopLoss;

	private TradeContextData(Double powerReserve,
	                        PositionType positionType,
	                        EstimationType estimationType,
	                        Double level,
	                        Double goal,
	                        Double slippage,
	                        Double price,
	                        Double techStopLoss) {
		this.powerReserve = powerReserve;
		this.positionType = positionType;
		this.estimationType = estimationType;
		this.level = level;
		this.goal = goal;
		this.slippage = slippage;
		this.price = price;
		this.techStopLoss = techStopLoss;
	}

	public Double getPowerReserve() {
		return powerReserve;
	}

	public PositionType getPositionType() {
		return positionType;
	}

	public EstimationType getEstimationType() {
		return estimationType;
	}

	public Double getLevel() {
		return level;
	}

	public Double getGoal() {
		return goal;
	}

	public Double getSlippage() {
		return slippage;
	}

	public Double getPrice() {
		return price;
	}

	public Double getTechStopLoss() {
		return techStopLoss;
	}


	public static final class TradeContextDataBuilder {
		private Double         powerReserve;
		private PositionType   positionType;
		private EstimationType estimationType;
		private Double         level;
		private Double         goal;
		private Double         slippage;
		private Double         price;
		private Double         techStopLoss;

		private TradeContextDataBuilder() {
		}

		public static TradeContextDataBuilder aTradeContextData() {
			return new TradeContextDataBuilder();
		}

		public TradeContextDataBuilder withPowerReserve(Double powerReserve) {
			this.powerReserve = powerReserve;
			return this;
		}

		public TradeContextDataBuilder withPositionType(PositionType positionType) {
			this.positionType = positionType;
			return this;
		}

		public TradeContextDataBuilder withEstimationType(EstimationType estimationType) {
			this.estimationType = estimationType;
			return this;
		}

		public TradeContextDataBuilder withLevel(Double level) {
			this.level = level;
			return this;
		}

		public TradeContextDataBuilder withGoal(Double goal) {
			this.goal = goal;
			return this;
		}

		public TradeContextDataBuilder withSlippage(Double slippage) {
			this.slippage = slippage;
			return this;
		}

		public TradeContextDataBuilder withPrice(Double price) {
			this.price = price;
			return this;
		}

		public TradeContextDataBuilder withTechStopLoss(Double techStopLoss) {
			this.techStopLoss = techStopLoss;
			return this;
		}

		public TradeContextData build() {
			return new TradeContextData(powerReserve,
			                            positionType,
			                            estimationType,
			                            level,
			                            goal,
			                            slippage,
			                            price,
			                            techStopLoss);
		}
	}
}
