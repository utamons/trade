package com.corn.trade.trade.analysis;

import com.corn.trade.trade.type.EstimationType;
import com.corn.trade.trade.type.PositionType;
import com.corn.trade.trade.analysis.data.TradeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.corn.trade.BaseWindow.*;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.round;
import static java.lang.Math.abs;

public class TradeCalc {
	private final Logger log = LoggerFactory.getLogger(TradeCalc.class);

	private final TradeData tradeData;
	private       double    reference;
	private       int       quantity;
	private       double    orderLimit;
	private       double    orderStop;
	private       double    stopLoss;
	private       double    takeProfit;
	private       double    breakEven;

	private double risk;
	private double outputExpected;
	private double riskPercent;
	private double gain;
	private double riskRewardRatioPercent;
	private String tradeError;

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

	public TradeData calculate() {
		estimate();
		return tradeData.toBuilder()
		                .withQuantity(quantity)
		                .withOrderLimit(orderLimit)
		                .withOrderStop(orderStop)
		                .withStopLoss(stopLoss)
		                .withTakeProfit(takeProfit)
		                .withBreakEven(breakEven)
		                .withRisk(risk)
		                .withOutputExpected(outputExpected)
		                .withRiskPercent(riskPercent)
		                .withGain(gain)
		                .withTradeError(tradeError)
		                .withRiskRewardRatioPercent(riskRewardRatioPercent)
		                .build();
	}

	private double getMinPowerReserve(TradeData tradeData) {
		double minPowerReserve = 0;
		TradeData temp = tradeData.toBuilder().withEstimationType(EstimationType.MAX_STOP_LOSS).build();
		do {
			minPowerReserve = round(minPowerReserve + 0.01);
			temp = temp.toBuilder().withPowerReserve(minPowerReserve).build();
			TradeCalc tradeCalc = new TradeCalc(temp);
			temp = tradeCalc.calculate();
		} while (temp.getTradeError() != null && minPowerReserve/temp.getPrice() < 0.5); // 50% of price is a reasonable maximum
		return minPowerReserve;
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
		if (tradeData.getTechStopLoss() != null &&
		    tradeData.getPositionType() == PositionType.LONG &&
		    tradeData.getTechStopLoss() >= tradeData.getLevel()) {
			throw new IllegalArgumentException("TechStopLoss must be below the level.");
		}
		if (tradeData.getTechStopLoss() != null &&
		    tradeData.getPositionType() == PositionType.SHORT &&
		    tradeData.getTechStopLoss() <= tradeData.getLevel()) {
			throw new IllegalArgumentException("TechStopLoss must be above the level.");
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
		} else {
			// Set minimum powerReserve
			double minPowerReserve = getMinPowerReserve(tradeData);
			builder.withPowerReserve(minPowerReserve);
			Double calculatedGoal = calculateGoal(tradeData, minPowerReserve);
			builder.withGoal(calculatedGoal);
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

	private double getTaxes(double sum) {
		return sum * 0.1; // ИПН
	}

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

		double range   = abs(priceClose - price);
		double percent = range / powerReserve() * 100;

		log.debug("BE: {}, range {}, {}%", fmt(priceClose), fmt(range), fmt(percent));

		return priceClose;
	}

	public void estimate() {

		fillQuantity();
		fillOrder();

		tradeError = null;
		int counter = 0;
		do {
			stopLoss = 0;
			do {
				counter++;
				breakEven = getBreakEven(orderLimit);
				double reward = getReward();
				risk = getRisk(reward);
				if (stopLoss == 0)
					stopLoss = round(calculateStopLoss());
				// recalculate risk because stop loss might be corrected by slippage
				risk = recalculatedRisk();

				fillTradeAndRiskFields(reward);

				log.debug(
						"Iteration {} - quantity: {}, take profit: {}, stop loss {}, risk: {}, reward: {}, risk reward " +
						"risk {}% ratio: {}",
						counter,
						quantity,
						fmt(takeProfit),
						fmt(stopLoss),
						fmt(risk),
						fmt(outputExpected),
						fmt(riskPercent),
						fmt(riskRewardRatioPercent)
				);

				if (stopLossTooSmall()) {
					break;
				}

				if (tradeData.getTechStopLoss() == null && (riskPercent > MAX_RISK_PERCENT || round(riskRewardRatioPercent) > round(1 / MAX_RISK_REWARD_RATIO * 100)))
					stopLoss = stopLoss + 0.01 * (isLong() ? 1 : -1);
				else
					break;

			} while (
					(riskPercent > MAX_RISK_PERCENT ||
					 round(riskRewardRatioPercent) > round(1 / MAX_RISK_REWARD_RATIO * 100)) &&
					stopLoss > 0 &&
					((isLong() && takeProfit > breakEven) || (isShort() && takeProfit < breakEven))
			);
			if (areRiskLimitsFailed() != null)
				quantity--;
		} while (areRiskLimitsFailed() != null && quantity > 0);

		tradeError = areRiskLimitsFailed();
	}

	private boolean stopLossTooSmall() {
		return (isLong() && round(stopLoss) >= tradeData.getLevel()) ||
		       (isShort() && round(stopLoss) <= tradeData.getLevel());
	}

	public Double getCorrectedStopLoss() {
		return isLong() ? round(stopLoss - slippage()) : round(stopLoss + slippage());
	}

	private void fillTradeAndRiskFields(double reward) {
		outputExpected = reward * quantity;
		gain = outputExpected / (orderLimit * quantity) * 100;
		risk = risk * quantity;
		riskRewardRatioPercent = risk / outputExpected * 100;
		riskPercent = risk / MAX_VOLUME * 100;
	}

	private double getRisk(double reward) {
		return reward / MAX_RISK_REWARD_RATIO;
	}

	private double recalculatedRisk() {
		return isLong() ? breakEven - getCorrectedStopLoss() : getCorrectedStopLoss() - breakEven;
	}

	private double getReward() {
		return isLong() ? takeProfit - breakEven : breakEven - takeProfit;
	}

	private double calculateStopLoss() {
		if (tradeData.getTechStopLoss() != null) {
			return tradeData.getTechStopLoss();
		}
		double stopLoss = isLong() ? breakEven - risk : breakEven + risk;

		if (estimationType() == EstimationType.MIN_STOP_LOSS) {
			double minStopLoss =
					isLong() ? getTradeData().getLevel() - ORDER_LUFT : getTradeData().getLevel() + ORDER_LUFT;
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private String areRiskLimitsFailed() {
		if ((isLong() && takeProfit <= breakEven) || (isShort() && takeProfit >= breakEven)) {
			return String.format("Take profit %.2f is less than break even %.2f", takeProfit, breakEven);
		}
		if (quantity <= 0) {
			return String.format("Quantity %d is less than 0", quantity);
		}
		if (stopLossTooSmall()) {
			String direction = isLong() ? "above" : "below";
			return String.format("Stop loss %.2f is " + direction + " the level", stopLoss);
		}
		if (riskPercent > MAX_RISK_PERCENT) {
			return String.format("Risk percent %.2f is greater than %.2f", riskPercent, MAX_RISK_PERCENT);
		}
		if (round(riskRewardRatioPercent) > round(1 / MAX_RISK_REWARD_RATIO * 100)) {
			return String.format("Risk reward ratio percent %.2f is greater than %.2f", riskRewardRatioPercent,
			                     round(1 / MAX_RISK_REWARD_RATIO * 100));
		}
		return null;
	}

	private void fillQuantity() {
		if (quantity == 0)
			quantity = maxQuantity();
	}

	private void fillOrder() {
		orderStop = reference;
		orderLimit = orderStop +
		             (isLong() ? tradeData.getSlippage() + tradeData.getLuft() : -tradeData.getSlippage() -
		                                                                         tradeData.getLuft());
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

	private EstimationType estimationType() {
		return tradeData.getEstimationType();
	}

	private double slippage() {
		return tradeData.getSlippage();
	}

	private double powerReserve() {
		return tradeData.getPowerReserve();
	}

}
