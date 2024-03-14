package com.corn.trade.trade.analysis;

import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeCalc {
	private final Logger log = LoggerFactory.getLogger(TradeCalc.class);

	public void calculate(TradeData tradeData) {
		validateAndComplement(tradeData);
	}

	public TradeData validateAndComplement(TradeData tradeData) {
		if (tradeData.getLevel() == null) {
			throw new IllegalArgumentException("Level is required");
		}
		if (tradeData.getPrice() == null) {
			throw new IllegalArgumentException("Price is required");
		}
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL &&
		    tradeData.getPowerReserve() == null &&
		    tradeData.getGoal() == null) {
			throw new IllegalArgumentException("Goal or Power reserve is required");
		}
		// Validate non-null double fields and ensure they are not less than zero
		if (tradeData.getLevel() < 0) {
			throw new IllegalArgumentException("Level cannot be less than zero.");
		}
		if (tradeData.getPrice() < 0) {
			throw new IllegalArgumentException("Price cannot be less than zero.");
		}
		if (tradeData.getPowerReserve() != null && tradeData.getPowerReserve() < 0) {
			throw new IllegalArgumentException("Power reserve cannot be less than zero.");
		}
		if (tradeData.getGoal() != null && tradeData.getGoal() < 0) {
			throw new IllegalArgumentException("Goal cannot be less than zero.");
		}
		if (tradeData.getTechStopLoss() != null && tradeData.getTechStopLoss() < 0) {
			throw new IllegalArgumentException("TechStopLoss cannot be less than zero.");
		}
		if (tradeData.getLuft() == null || tradeData.getLuft() < 0) {
			throw new IllegalArgumentException("Luft is required and cannot be less than zero.");
		}
		if (tradeData.getSlippage() != null && tradeData.getSlippage() < 0) {
			throw new IllegalArgumentException("Slippage cannot be less than zero.");
		}

		// Goal validation based on PositionType
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL &&
		    tradeData.getGoal() != null &&
		    tradeData.getPrice() != null &&
		    tradeData.getPowerReserve() == null) {
			if (tradeData.getPositionType() == PositionType.LONG && tradeData.getGoal() <= tradeData.getPrice()) {
				throw new IllegalArgumentException("Goal cannot be equal or less than price for LONG position.");
			} else if (tradeData.getPositionType() == PositionType.SHORT && tradeData.getGoal() >= tradeData.getPrice()) {
				throw new IllegalArgumentException("Goal cannot be equal or greater than price for SHORT position.");
			}
		}

		// Goal and Level relationship validation
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL &&
		    tradeData.getGoal() != null &&
		    tradeData.getLevel() != null &&
		    tradeData.getPowerReserve() == null) {
			if (tradeData.getPositionType() == PositionType.LONG && tradeData.getGoal() <= tradeData.getLevel()) {
				throw new IllegalArgumentException("Goal cannot be equal or less than level for LONG position.");
			} else if (tradeData.getPositionType() == PositionType.SHORT && tradeData.getGoal() >= tradeData.getLevel()) {
				throw new IllegalArgumentException("Goal cannot be equal or greater than level for SHORT position.");
			}
		}

		// Set default slippage if undefined
		Double slippage = (tradeData.getSlippage() == null) ? tradeData.getLuft() : tradeData.getSlippage();

		// Update the TradeData object with calculated values or defaults
		TradeData.Builder builder = tradeData.toBuilder().withSlippage(slippage);

		// Computation for goal and powerReserve
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL) {
			if (tradeData.getPowerReserve() != null) {
				// Calculate and set goal
				Double calculatedGoal = calculateGoal(tradeData, tradeData.getPowerReserve());
				builder.withGoal(calculatedGoal);
			} else if (tradeData.getGoal() != null && tradeData.getPowerReserve() == null) {
				// Calculate and set powerReserve
				Double calculatedPowerReserve = calculatePowerReserve(tradeData, tradeData.getGoal());
				builder.withPowerReserve(calculatedPowerReserve);
			}
		}

		return builder.build();
	}

	private Double calculateGoal(TradeData tradeData, Double powerReserve) {
		double reference;

		// Determine the reference point: use the highest of level or price for LONG, and lower for SHORT
		if (tradeData.getPositionType() == PositionType.LONG) {
			reference = Math.max(tradeData.getPrice(),
			                     tradeData.getLevel() != null ? tradeData.getLevel() : tradeData.getPrice());
			return reference + powerReserve;
		} else if (tradeData.getPositionType() == PositionType.SHORT) {
			reference = Math.min(tradeData.getPrice(),
			                     tradeData.getLevel() != null ? tradeData.getLevel() : tradeData.getPrice());
			return reference - powerReserve;
		}

		return null;
	}

	private Double calculatePowerReserve(TradeData tradeData, Double goal) {
		double reference;

		// Determine the reference point: use the highest of level or price for LONG, and lower for SHORT
		if (tradeData.getPositionType() == PositionType.LONG) {
			reference = Math.max(tradeData.getPrice(),
			                     tradeData.getLevel() != null ? tradeData.getLevel() : tradeData.getPrice());
			// For LONG, power reserve is the distance from the reference to the goal
			return goal - reference;
		} else if (tradeData.getPositionType() == PositionType.SHORT) {
			reference = Math.min(tradeData.getPrice(),
			                     tradeData.getLevel() != null ? tradeData.getLevel() : tradeData.getPrice());
			// For SHORT, power reserve is the distance from the goal to the reference
			return reference - goal;
		}

		return null; // This should not happen if positionType is always either LONG or SHORT
	}

}
