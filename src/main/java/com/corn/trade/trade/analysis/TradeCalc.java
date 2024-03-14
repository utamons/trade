package com.corn.trade.trade.analysis;

import com.corn.trade.trade.EstimationType;
import com.corn.trade.trade.PositionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.corn.trade.BaseWindow.MAX_VOLUME;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.showErrorDlg;

public class TradeCalc {
	private final Logger log = LoggerFactory.getLogger(TradeCalc.class);

	private final TradeData tradeData;
	private       double    reference;
	private       double    quantity;
	private       double    orderLimit;
	private       double    orderStop;
	private       double    stopLoss;
	private       double    takeProfit;
	private       double    breakEven;

	public TradeCalc(TradeData tradeData) {
		this.tradeData = validateAndComplement(tradeData);
	}

	@SuppressWarnings("unused")
	private TradeCalc() {
		this.tradeData = null;
	}

	public TradeData getTradeData() {
		return tradeData;
	}

	public void calculate() {

	}

	private TradeData validateAndComplement(TradeData tradeData) {
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

		reference = getReferencePoint(tradeData);
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

	private double getReferencePoint(TradeData tradeData) {
		if (tradeData.getPositionType() == PositionType.LONG) {
			return Math.max(tradeData.getPrice(), tradeData.getLevel());
		} else if (tradeData.getPositionType() == PositionType.SHORT) {
			return Math.min(tradeData.getPrice(), tradeData.getLevel());
		}
		return 0.0;
	}

	private Double calculateGoal(TradeData tradeData, Double powerReserve) {
		if (tradeData.getPositionType() == PositionType.LONG) {
			return reference + powerReserve;
		} else if (tradeData.getPositionType() == PositionType.SHORT) {
			return reference - powerReserve;
		}
		return null;
	}


	private Double calculatePowerReserve(TradeData tradeData, Double goal) {
		if (tradeData.getPositionType() == PositionType.LONG) {
			return goal - reference;
		} else if (tradeData.getPositionType() == PositionType.SHORT) {
			return reference - goal;
		}
		return null;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit <= breakEven) || (isShort() && takeProfit >= breakEven)) {
			log.debug("RL: Take profit {} is less than break even {}", fmt(takeProfit), fmt(breakEven));
			return true;
		}
		if (quantity <= 0) {
			log.debug("RL: Quantity {} is less than 0", quantity);
			return true;
		}
		if (stopLossTooLow()) {
			log.debug(2, "RL: Stop loss {} is too low", fmt(getCorrectedStopLoss()));
			return true;
		}
		return false;
	}

	private void fillQuantity() {
		if (quantity == 0 ||
		    tradeData.getEstimationType() == EstimationType.MAX_STOP_LOSS ||
		    tradeData.getEstimationType() == EstimationType.MIN_STOP_LOSS)
			quantity = maxQuantity();
	}

	private void fillOrder() {
		orderStop = reference;
		orderLimit = orderStop + (isLong() ? tradeData.getSlippage() * tradeData.getLuft() : -tradeData.getSlippage() * tradeData.getLuft());
		takeProfit = isLong() ? reference + tradeData.getPowerReserve() : reference - tradeData.getPowerReserve();
	}

	private boolean isShort() {
		return !isLong();
	}

	private boolean isLong() {
		return tradeData.getPositionType().equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / reference);
	}

}
