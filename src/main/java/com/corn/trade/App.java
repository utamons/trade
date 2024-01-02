package com.corn.trade;

import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.ibkr.Ibkr;
import com.corn.trade.ibkr.OrderHelper;
import com.corn.trade.ibkr.PositionHelper;
import com.corn.trade.panel.*;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.corn.trade.util.Util.showWarningDlg;

public class App {
	public static final  Theme  DARK_THEME   = new OneDarkTheme();
	public static final  Theme  LIGHT_THEME  = new IntelliJTheme();
	public static final  int    PREF_HEIGHT  = 250;
	public static final  int    PREF_WIDTH   = 330;
	private static final Logger log          = LoggerFactory.getLogger(App.class);
	private static final int    FIELD_HEIGHT = 40;
	public static        int    DEBUG_LEVEL  = 1;

	public static double MAX_VOLUME            = 2000.0;
	public static double MAX_RISK_PERCENT      = 0.3;
	public static double MAX_RISK_REWARD_RATIO = 3.0;
	public static double ORDER_LUFT            = 0.02;

	public static Properties loadProperties(String fileName) {
		Properties props = new Properties();
		try (InputStream input = new FileInputStream(fileName)) {
			props.load(input);
		} catch (IOException ex) {
			log.error("Error loading properties file: {}, {}", fileName, ex.getMessage());
		}
		return props;
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			log.info("Calculator version 1.0.1");

			setDefaultFont();

			JFrame frame = new JFrame("Trade Calculator v. 1.0.1");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 630);

			Ibkr ibkr = new Ibkr();
			ibkr.run();

			Levels     levels     = new Levels(frame);
			Calculator calculator = new Calculator(frame, levels);
			AutoUpdate autoUpdate = new AutoUpdate(frame, ibkr);
			autoUpdate.addActivateListener(calculator::setAutoUpdate);

			OrderHelper    orderHelper    = new OrderHelper(ibkr);
			PositionHelper positionHelper = new PositionHelper(ibkr);

			InputPanel inputPanel = new InputPanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT * 2),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					5, FIELD_HEIGHT);
			PowerPanel powerPanel = new PowerPanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT - 30),
					new Dimension(PREF_WIDTH, PREF_HEIGHT - 30),
					5, FIELD_HEIGHT);
			TradePanel tradePanel = new TradePanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					5, FIELD_HEIGHT);
			RiskPanel riskPanel = new RiskPanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT - 80),
					new Dimension(PREF_WIDTH, PREF_HEIGHT - 80),
					5, FIELD_HEIGHT);
			OrderPanel orderPanel = new OrderPanel(
					calculator,
					autoUpdate,
					orderHelper,
					positionHelper,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					5, FIELD_HEIGHT);

			JPanel mainContainer = new JPanel();
			mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

			mainContainer.add(inputPanel);
			mainContainer.add(orderPanel);
			mainContainer.add(tradePanel);
			mainContainer.add(riskPanel);
			mainContainer.add(powerPanel);

			JScrollPane scrollPane = new JScrollPane(mainContainer,
			                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			frame.getContentPane().add(scrollPane);

			frame.setLocationRelativeTo(null);

			LafManager.enabledPreferenceChangeReporting(true);
			LafManager.addThemePreferenceChangeListener(new CustomThemeListener());
			LafManager.setThemeProvider(new DefaultThemeProvider(
					LIGHT_THEME,
					DARK_THEME,
					new HighContrastLightTheme(),
					new HighContrastDarkTheme()
			));

			frame.pack();

			frame.setVisible(true);

			Properties configProps = loadProperties("D:\\bin\\trade.properties");

			MAX_VOLUME = Integer.parseInt(configProps.getProperty("max_volume", "2000")); // Default value is 1000
			MAX_RISK_REWARD_RATIO =
					Double.parseDouble(configProps.getProperty("max_risk_reward_ratio", "3")); // Default value is 1.5
			MAX_RISK_PERCENT = Double.parseDouble(configProps.getProperty("max_risk_percent", "0.5")); // Default value is 2
			ORDER_LUFT = Double.parseDouble(configProps.getProperty("order_luft", "0.02")); // Default value is 10
			DEBUG_LEVEL = Integer.parseInt(configProps.getProperty("debug_level", "2")); // Default value is 1
			log.info("max volume: {}", MAX_VOLUME);
			log.info("max risk/reward ratio: {}", MAX_RISK_REWARD_RATIO);
			log.info("max risk: {}%", MAX_RISK_PERCENT);
			log.info("order luft: {}", ORDER_LUFT);

			log.info("Application started");

			if (!ibkr.isConnected()) {
				showWarningDlg(frame, "Not connected to IBKR. Auto update and orders will be simulated!");
				log.warn("Not connected to IBKR. Auto update and orders will be simulated!");
			}
		});
	}

	private static void setDefaultFont() {
		for (Object key : UIManager.getDefaults().keySet()) {
			if (UIManager.get(key) instanceof Font) {
				UIManager.put(key, new Font("Arial", Font.PLAIN, 14));
			}
		}
	}

	static class CustomThemeListener implements ThemePreferenceListener {

		public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
			LafManager.installTheme(e.getPreferredThemeStyle());
		}
	}
}
