package com.corn.trade.trade;

import com.corn.trade.common.Notifier;
import com.corn.trade.panel.calculator.ColorfulTextWindow;
import com.corn.trade.panel.calculator.ParamPanel;
import com.corn.trade.util.Debug;
import com.corn.trade.util.LiquibaseRunner;
import com.corn.trade.util.Util;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.HighContrastDarkTheme;
import com.github.weisj.darklaf.theme.HighContrastLightTheme;
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.corn.trade.Trade.*;
import static com.corn.trade.util.Util.fmt;
import static com.corn.trade.util.Util.showErrorDlg;
import static java.lang.Math.abs;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Calculator extends Notifier {
	public static final double SPREAD_COEFF = 2;

	public final  Debug     log = new Debug(LoggerFactory.getLogger(Calculator.class));
	private final Component frame;
	private final Levels    levels;
	private final List<Double>     spreads       = new ArrayList<>();
	private       boolean        autoUpdate    = false;
	private       PositionType   positionType;
	private       EstimationType estimationType;
	private       Double         outputExpected;
	private       Double         gain;
	private       Double         spread;
	private       boolean        spreadError   = false;
	private       Double         stopLoss;
	private       Double         takeProfit;
	private       Double         breakEven;
	private       Double         risk;
	private       Double         riskPercent;
	private       Double         riskRewardRatioPercent;
	private       Double         orderLimit;
	private       Double         orderStop;
	private       Integer        quantity;
	private       boolean        quantityError = false;

	private boolean tradeError = false;

	private boolean yellowLight = false;

	public Calculator(Component frame, Levels levels) {
		this.frame = frame;
		this.levels = levels;
	}

	public boolean isYellowLight() {
		return yellowLight;
	}

	public boolean isTradeError() {
		return tradeError;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isSpreadError() {
		return spreadError;
	}

	public boolean isQuantityError() {
		return quantityError;
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

	public void setSpread(Double newSpread) {
		if (!autoUpdate) {
			spread = newSpread;
			return;
		}
		// Add the new spread to the list
		spreads.add(newSpread);

		// Ensure the list only keeps the last 10 spreads
		if (spreads.size() > 10) {
			spreads.remove(0); // Remove the oldest spread to maintain the size limit
		}

		// Calculate the average of the spreads in the list

		// Set the spread field to the calculated average
		this.spread = spreads.stream()
		                     .mapToDouble(Double::doubleValue)
		                     .average()
		                     .orElse(0.0);
	}


	public Double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getCorrectedStopLoss() {
		if (stopLoss == null)
			return 0.0;
		return isLong() ? Util.round( stopLoss + spread) : Util.round(stopLoss - spread);
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
		int    counter         = 0;

		while (profit < openCommission + closeCommission + taxes) {
			priceClose = priceClose + increment;
			closeCommission = estimatedCommissionUSD(priceClose);
			profit = abs(priceClose - price) * quantity;
			taxes = getTaxes(profit);
			counter++;
		}

		double range   = abs(priceClose - price);
		double percent = range / levels.getPowerReserve() * 100;

		log.debug(2, "BE: {}, range {}, {}%", fmt(priceClose), fmt(range), fmt(percent));

		return priceClose;
	}

	private double getTaxes(double sum) {
		return sum * 0.1; // ИПН
	}

	private boolean isValidEstimation() {
		quantityError = false;
		String error = levels.validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			log.error("Error: {}", error);
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
		if (levels.getPivotPoint() == null) {
			levels.calculatePivotPoint(positionType);
		}
		if (levels.getPowerReserve() == null) {
			levels.calculatePowerReserve(positionType);
		}
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
		}

		return error == null;
	}

	public void checkYellowLight() {
		yellowLight = false;
		if (riskPercent == null || takeProfit == null || breakEven == null)
			return;
		if (Math.abs(takeProfit - breakEven) < 0.05) {
			log.debug(2, "YL: Take profit {} is too close to break even {}", fmt(takeProfit), fmt(breakEven));
			yellowLight = true;
		}
		if (!levels.isStopLossUnderLevels(getCorrectedStopLoss(), positionType)) {
			log.debug(2, "YL: Stop loss {} is not under levels", fmt(getCorrectedStopLoss()));
			yellowLight = true;
		}
	}

	public void estimate() {
		if (!isValidEstimation())
			return;

		levels.calculatePivotPoint(positionType);
		if (autoUpdate || levels.getPowerReserve() == null)
			levels.calculatePowerReserve(positionType);

		fillQuantity();
		fillOrder();

		tradeError = false;

		int counter = 0;
		do {
			counter++;
			breakEven = getBreakEven(orderLimit);
			double reward = getReward();
			risk = getRisk(reward);
			stopLoss = calculateStopLoss();
			// recalculate risk because stop loss might be corrected
			risk = recalculatedRisk();

			fillTradeAndRiskFields(reward);

			if (stopLossTooLow()) {
				break;
			}

			if (riskPercent > MAX_RISK_PERCENT)
				quantity--;
			log.debug(2,
			          "Iteration {} - quantity: {}, take profit: {}, stop loss {}, risk percent: {}, risk reward ratio: {}",
			          counter,
			          quantity,
			          fmt(takeProfit),
			          fmt(getCorrectedStopLoss()),
			          fmt(riskPercent),
			          fmt(riskRewardRatioPercent)
			);
		} while (
				riskPercent > MAX_RISK_PERCENT &&
				quantity > 0 &&
				((isLong() && takeProfit > breakEven) || (isShort() && takeProfit < breakEven))
		);

		if (areRiskLimitsFailed()) {
			tradeError = true;
		}
		checkYellowLight();

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
		return isLong() ? breakEven - stopLoss : stopLoss - breakEven;
	}

	private double getReward() {
		return isLong() ? takeProfit - breakEven : breakEven - takeProfit;
	}

	private void decreaseTakeProfit() {
		takeProfit = isLong() ? takeProfit - 0.01 : takeProfit + 0.01;
	}

	private double calculateStopLoss() {
		double stopLoss = isLong() ? breakEven - risk : breakEven + risk;

		if (estimationType == EstimationType.MIN_STOP_LOSS ||
		    estimationType == EstimationType.MAX_GAIN_MIN_STOP_LOSS) {
			double minStopLoss =
					isLong() ? levels.getPivotPoint() - Math.max(ORDER_LUFT, spread * 2) : levels.getPivotPoint() +
					                                                                   Math.max(ORDER_LUFT, spread * 2);
			return isLong() ? Math.max(stopLoss, minStopLoss) : Math.min(stopLoss, minStopLoss);
		}

		return stopLoss;
	}

	private boolean areRiskLimitsFailed() {
		if ((isLong() && takeProfit <= breakEven) || (isShort() && takeProfit >= breakEven)) {
			log.debug(2, "RL: Take profit {} is less than break even {}", fmt(takeProfit), fmt(breakEven));
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			return true;
		}
		if (quantity <= 0) {
			log.debug(2, "RL: Quantity {} is less than 0", quantity);
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
			return true;
		}
		if (stopLossTooLow()) {
			log.debug(2, "RL: Stop loss {} is too low", fmt(getCorrectedStopLoss()));
			showErrorDlg(frame, "Cannot fit to risk limits!", !autoUpdate);
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
		orderStop = levels.getPivotPoint();
		orderLimit = orderStop + (isLong() ? spread * SPREAD_COEFF : -spread * SPREAD_COEFF);
		takeProfit = isLong() ? levels.getPivotPoint() + levels.getPowerReserve() : levels.getPivotPoint() -
		                                                                            levels.getPowerReserve();
	}

	private boolean stopLossTooLow() {
		return (isLong() && getCorrectedStopLoss() > levels.getPivotPoint() - Math.max(ORDER_LUFT, spread)) ||
		       (isShort() && getCorrectedStopLoss() < levels.getPivotPoint() + Math.max(ORDER_LUFT, spread));
	}

	private boolean isLong() {
		return positionType.equals(PositionType.LONG);
	}

	private int maxQuantity() {
		return (int) (MAX_VOLUME / levels.getPivotPoint());
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
		quantityError = false;
		spreadError = false;
		announce();
	}

	public static void main(String[] args) {
		final String stage = args.length>0 && args[0].equals("-dev")? "dev" : "prod";

		final String dbKey = stage.equals("dev")? "db_url_dev" : "db_url_prod";

		SwingUtilities.invokeLater(() -> {

			Properties configProps = loadProperties("D:\\bin\\trade.properties");

			MAX_VOLUME = Integer.parseInt(configProps.getProperty("max_volume", "2000"));
			MAX_RISK_REWARD_RATIO =
					Double.parseDouble(configProps.getProperty("max_risk_reward_ratio", "3"));
			MAX_RISK_PERCENT = Double.parseDouble(configProps.getProperty("max_risk_percent", "0.5"));
			ORDER_LUFT = Double.parseDouble(configProps.getProperty("order_luft", "0.02"));
			DEBUG_LEVEL = Integer.parseInt(configProps.getProperty("debug_level", "2"));
			MIN_POWER_RESERVE_TO_PRICE_RATIO =
					Double.parseDouble(configProps.getProperty("min_power_reserve_to_price_ratio", "0.005"));
			REALISTIC_POWER_RESERVE = Double.parseDouble(configProps.getProperty("realistic_power_reserve", "1"));
			SIMULATION_MODE = Boolean.parseBoolean(configProps.getProperty("simulation_mode", "false"));
			DB_URL = configProps.getProperty(dbKey, null);
			DB_USER = configProps.getProperty("db_user", null);
			DB_PASSWORD = configProps.getProperty("db_password", null);

			LiquibaseRunner.runLiquibase();

			setDefaultFont();

			JFrame frame = new JFrame("Trade Calculator v. " + version + " (" + stage + ")");
			frame.setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 630);

			JPanel mainContainer = new JPanel();
			mainContainer.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			Levels     levels     = new Levels(frame);
			Calculator calculator = new Calculator(frame, levels);

			ParamPanel paramPanel = new ParamPanel(calculator, levels, new Dimension(650, 180), new Dimension(650,180),5, FIELD_HEIGHT);

			ColorfulTextWindow textWindow = new ColorfulTextWindow(new Dimension(650, 180));

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			gbc.weighty = 0; // No extra vertical space allocation
			gbc.fill = GridBagConstraints.HORIZONTAL;
			mainContainer.add(paramPanel, gbc);

			gbc.gridy = 1;
			gbc.weighty = 1; // Allocate extra space vertically to this component
			gbc.fill = GridBagConstraints.BOTH; // Allow stretching both horizontally and vertically
			mainContainer.add(textWindow, gbc);

			textWindow.appendText("Trade Calculator v. " + version + " (" + stage + ")");
			textWindow.appendText("Enter the parameters and press 'Estimate' to calculate the trade");
			textWindow.appendText("A green line", Color.GREEN.darker(), true);
			textWindow.appendText("Indicates a good trade, a red line", Color.RED.darker(), true);
			textWindow.appendText("Indicates a bad trade");

			frame.getContentPane().add(mainContainer, BorderLayout.CENTER);

			frame.setLocationRelativeTo(null);

			LafManager.enabledPreferenceChangeReporting(true);
			LafManager.setDecorationsEnabled(false);
			LafManager.addThemePreferenceChangeListener(new CustomThemeListener());
			LafManager.setThemeProvider(new DefaultThemeProvider(
					LIGHT_THEME,
					DARK_THEME,
					new HighContrastLightTheme(),
					new HighContrastDarkTheme()
			));

			frame.pack();

			frame.setVisible(true);
		});
	}
}
