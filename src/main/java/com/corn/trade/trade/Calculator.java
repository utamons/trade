package com.corn.trade.trade;

import com.corn.trade.common.Notifier;

import java.awt.*;

import static com.corn.trade.util.Util.log;
import static com.corn.trade.util.Util.showErrorDlg;
import static java.lang.Math.abs;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Calculator extends Notifier {
	private final Double         MAX_VOLUME            = 5000.0;
	private final double         MAX_RISK_PERCENT      = 0.5;
	private final double         MAX_RISK_REWARD_RATIO = 3.0;
	private final Double         ORDER_LUFT            = 0.02;
	private final Component      frame;
	private final Levels         levels;
	private       boolean        autoUpdate            = false;
	private       PositionType   positionType;
	private       EstimationType estimationType;
	private       Double         outputExpected;
	private       Double         gain;
	private       Double         spread;
	private       Double         pivotPoint;
	private       Double         powerReserve;
	private       boolean        spreadError           = false;
	private       Double         stopLoss;
	private       boolean        stopLossError         = false;
	private       Double         takeProfit;
	private       boolean        takeProfitError       = false;
	private       Double         breakEven;
	private       Double         risk;
	private       Double         riskPercent;
	private       Double         riskRewardRatioPercent;
	private       Double         orderLimit;
	private       Double         orderStop;
	private       Integer        quantity;
	private       boolean        quantityError         = false;

	public Calculator(Component frame, Levels levels) {
		this.frame = frame;
		this.levels = levels;
	}

	public void setPivotPoint(Double pivotPoint) {
		this.pivotPoint = pivotPoint;
	}

	public void setPowerReserve(Double powerReserve) {
		this.powerReserve = powerReserve;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isSpreadError() {
		return spreadError;
	}

	public boolean isStopLossError() {
		return stopLossError;
	}

	public boolean isQuantityError() {
		return quantityError;
	}

	public boolean isTakeProfitError() {
		return takeProfitError;
	}

	public Double getOutputExpected() {
		return outputExpected;
	}

	public Double getGain() {
		return gain;
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity == null ? null : quantity.intValue();
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

	private boolean isValidEstimation() {
		takeProfitError = false;
		stopLossError = false;
		spreadError = false;
		quantityError = false;
		String error = levels.validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return false;
		}
		if (spread == null) {
			spreadError = true;
			error = "Spread must be set\n ";
		} else if (spread <= 0) {
			spreadError = true;
			error = "Spread must be greater than 0\n ";
		}
		if (quantity != null && quantity <= 0) {
			quantityError = true;
			error = "Quantity must be greater than 0\n ";
		}
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
		}
		return error == null;
	}

	public void estimate() {
		if (!isValidEstimation())
			return;

		fillQuantity();
		fillOrder();

		do {
			breakEven = getBreakEven(orderLimit);
			if (areRiskLimitsFailed())
				return;
			double reward = getReward();
			risk = getRisk(reward);
			stopLoss = calculateStopLoss();

			// recalculate risk because stop loss might be corrected
			risk = recalculatedRisk();

			if (stopLossTooLow()) {
				decreaseTakeProfit();
			}

			fillTradeAndRiskFields(reward);

			if (riskPercent > MAX_RISK_PERCENT)
				quantity--;
		} while (riskPercent > MAX_RISK_PERCENT || stopLossTooLow());

		announce();
	}

	private void fillTradeAndRiskFields(double reward) {
		riskRewardRatioPercent = risk / reward * 100;
		outputExpected = reward * quantity;
		gain = outputExpected / (orderLimit * quantity) * 100;
		risk = risk * quantity;
		riskPercent = risk / MAX_VOLUME * 100;
	}

	private double getRisk(double reward) {
		return reward / MAX_RISK_REWARD_RATIO;
	}

	private double recalculatedRisk() {
		return isLong() ? breakEven - stopLoss + spread : stopLoss - breakEven + spread;
	}

	private double getReward() {
		return isLong() ? takeProfit - breakEven : breakEven - takeProfit;
	}

	private void decreaseTakeProfit() {
		takeProfit = isLong() ? takeProfit - 0.01 : takeProfit + 0.01;
	}

	private double calculateStopLoss() {
		/*
		 * The stop loss is corrected by the spread because the stop loss order might be executed by the worst price
		 * and this value should be a base for further calculations.
		 */
		double stopLoss = isLong() ? breakEven - risk + spread : breakEven + risk - spread;

		if (estimationType == EstimationType.MIN_STOP_LOSS ||
		    estimationType == EstimationType.MAX_GAIN_MIN_STOP_LOSS) {
			double minStopLoss =
					isLong() ? pivotPoint - Math.max(ORDER_LUFT, spread) : pivotPoint +
					                                                       Math.max(ORDER_LUFT, spread);
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit < breakEven) || (isShort() && takeProfit > breakEven)) {
			log("Take profit is less than break even");
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			takeProfitError = true;
			stopLossError = stopLossTooLow();
			gain = 0.0;
			outputExpected = 0.0;
			risk = 0.0;
			riskPercent = 0.0;
			riskRewardRatioPercent = 0.0;
			announce();
			return true;
		}
		if (quantity <= 0) {
			log("Cannot fit to risk limits");
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			announce();
			return true;
		}
		return false;
	}

	private boolean isShort() {
		return !isLong();
	}

	private void fillQuantity() {
		if (quantity == null ||
		    estimationType == EstimationType.MAX_GAIN_MAX_STOP_LOSS ||
		    estimationType == EstimationType.MAX_GAIN_MIN_STOP_LOSS)
			quantity = maxQuantity();
	}

	private void fillOrder() {
		orderStop = pivotPoint;
		orderLimit = orderStop + (isLong() ? spread : -spread);
		takeProfit = isLong() ? pivotPoint + powerReserve : pivotPoint - powerReserve;
	}

	private boolean stopLossTooLow() {
		return (isLong() && stopLoss > pivotPoint - Math.max(ORDER_LUFT, spread)) ||
		       (isShort() && stopLoss < pivotPoint + Math.max(ORDER_LUFT, spread));
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / pivotPoint);
	}

	@SuppressWarnings("DuplicatedCode")
	public void reset() {
		outputExpected = null;
		gain = null;
		stopLoss = null;
		takeProfit = null;
		breakEven = null;
		risk = null;
		riskPercent = null;
		riskRewardRatioPercent = null;
		orderLimit = null;
		orderStop = null;
		quantity = null;
		spread = null;
		takeProfitError = false;
		stopLossError = false;
		quantityError = false;
		spreadError = false;
		announce();
	}
}
