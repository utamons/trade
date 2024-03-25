package com.corn.trade.service;

import com.corn.trade.model.TradeData;
import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
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
		double    minPowerReserve = 0;
		TradeData temp            = tradeData.toBuilder().withEstimationType(EstimationType.MAX_STOP_LOSS).build();
		do {
			minPowerReserve = round(minPowerReserve + 0.01);
			temp = temp.toBuilder().withPowerReserve(minPowerReserve).build();
			TradeCalc tradeCalc = new TradeCalc(temp);
			temp = tradeCalc.calculate();
		} while (temp.getTradeError() != null &&
		         minPowerReserve / temp.getPrice() < 0.5); // 50% of price is a reasonable maximum
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
		    tradeData.getPrice() != null) {
			if (tradeData.getPositionType() == PositionType.LONG && tradeData.getGoal() <= tradeData.getPrice()) {
				throw new IllegalArgumentException("Goal must be greater than price!");
			} else if (tradeData.getPositionType() == PositionType.SHORT && tradeData.getGoal() >= tradeData.getPrice()) {
				throw new IllegalArgumentException("Goal must be less than price!");
			}
		}

		// Goal and Level relationship validation
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL &&
		    tradeData.getGoal() != null &&
		    tradeData.getLevel() != null) {
			if (tradeData.getPositionType() == PositionType.LONG && tradeData.getGoal() <= tradeData.getLevel()) {
				throw new IllegalArgumentException("Goal must be greater than level!");
			} else if (tradeData.getPositionType() == PositionType.SHORT && tradeData.getGoal() >= tradeData.getLevel()) {
				throw new IllegalArgumentException("Goal must be less than level!");
			}
		}

		return complement(tradeData);
	}

	private TradeData complement(TradeData tradeData) {
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

	private double getTax(double sum) {
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
		double taxes           = getTax(profit);
		double increment       = isLong() ? 0.01 : -0.01;

		while (profit < openCommission + closeCommission + taxes) {
			priceClose = priceClose + increment;
			closeCommission = estimatedCommissionUSD(priceClose);
			profit = abs(priceClose - price) * quantity;
			taxes = getTax(profit);
		}

		double range   = abs(priceClose - price);
		double percent = range / powerReserve() * 100;

		log.debug("BE: {}, range {}, {}%", fmt(priceClose), fmt(range), fmt(percent));

		return priceClose;
	}

	public void estimate() {
		tradeError = null;
		// fill order
		if (quantity == 0) quantity = maxQuantity();
		if (reference == tradeData.getLevel())
			orderStop = reference + (isLong() ? tradeData.getLuft() : -tradeData.getLuft());
		else orderStop = reference;

		orderLimit = orderStop +
		             (isLong() ? tradeData.getSlippage() + tradeData.getLuft() : -tradeData.getSlippage() -
		                                                                         tradeData.getLuft());
		takeProfit = tradeData.getGoal();

		do {
			breakEven = getBreakEven(orderLimit);

			double gross      = Math.abs(takeProfit - orderLimit) * quantity;
			double tax        = getTax(gross);
			double commission = estimatedCommissionUSD(orderLimit) + estimatedCommissionUSD(takeProfit);
			double net        = gross - tax - commission;
			risk = net * MAX_RISK_REWARD_RATIO;

			stopLoss = isLong() ? takeProfit - net/quantity - risk/quantity : takeProfit + net/quantity + risk/quantity;

			fillTradeAndRiskFields(net);

			log.debug("quantity: {}, take profit: {}, stop loss {}({}), risk: {}, reward: {}, " + "risk {}% rr: {}",
			          quantity,
			          fmt(takeProfit),
			          fmt(stopLoss),
			          fmt(correctedStopLoss(stopLoss)),
			          fmt(risk),
			          fmt(outputExpected),
			          fmt(riskPercent),
			          fmt(riskRewardRatioPercent));

			tradeError = areRiskLimitsFailed();
			log.debug(tradeError);
		} while (tradeError != null && (quantity = quantity - 1) > 0);
		if (tradeError == null) {
			stopLoss = tradeData.getTechStopLoss() != null? tradeData.getTechStopLoss() : correctedStopLoss(stopLoss);
		}
		log.debug("MAX_RISK_PERCENT: " + MAX_RISK_PERCENT);
		log.debug(tradeError);
	}

	private double correctedStopLoss(double sl) {
		return isLong() ? sl + slippage() : sl - slippage();
	}

	private boolean stopLossTooSmall() {
		double sl = round(correctedStopLoss(stopLoss));
		if (isLong() && tradeData.getTechStopLoss() != null) {
			return sl > tradeData.getTechStopLoss();
		} else if (isShort() && tradeData.getTechStopLoss() != null) {
			return sl < tradeData.getTechStopLoss();
		}

		return (isLong() && sl >= tradeData.getLevel()) ||
		       (isShort() && sl <= tradeData.getLevel());
	}

	private void fillTradeAndRiskFields(double reward) {
		outputExpected = reward;
		gain = outputExpected / (orderLimit * quantity) * 100;
		riskRewardRatioPercent = risk / outputExpected * 100;
		riskPercent = risk / MAX_VOLUME * 100;
	}

	private String areRiskLimitsFailed() {
		String riskFailed = "Doesn't meet risk limits";
		if ((isLong() && takeProfit <= breakEven) || (isShort() && takeProfit >= breakEven)) {
			return riskFailed + " 1";
		}
		if (quantity <= 0) {
			return riskFailed + " 2";
		}
		if (stopLossTooSmall()) {
			String direction = isLong() ? "above" : "below";
			return String.format("Stop loss %.2f is " + direction + " the level", correctedStopLoss(stopLoss));
		}
		if (riskPercent > MAX_RISK_PERCENT) {
			return riskFailed + " 3";
		}
		if (round(riskRewardRatioPercent) > round(1 / MAX_RISK_REWARD_RATIO * 100)) {
			return riskFailed + " 4";
		}
		return null;
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

	private double slippage() {
		return tradeData.getSlippage();
	}

	private double powerReserve() {
		return tradeData.getPowerReserve();
	}

}