package com.corn.trade;

import com.corn.trade.component.CustomTitleBar;
import com.corn.trade.component.ResizeListener;
import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.ibkr.Ibkr;
import com.corn.trade.ibkr.OrderHelper;
import com.corn.trade.ibkr.PositionHelper;
import com.corn.trade.panel.MainPanel;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;
import com.corn.trade.util.LiquibaseRunner;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.corn.trade.util.Util.showWarningDlg;

public class Trade {
	public final static String version = "2.0";
	public static final int RESIZE_EDGE = 4;
	public static final  Theme  DARK_THEME   = new OneDarkTheme();
	public static final  Theme  LIGHT_THEME  = new IntelliJTheme();
	public static final  int    PREF_HEIGHT  = 250;
	public static final  int    PREF_WIDTH   = 330;
	private static final Logger log          = LoggerFactory.getLogger(Trade.class);
	public static final  int    FIELD_HEIGHT = 40;
	public static        int    DEBUG_LEVEL  = 1;

	public static double MAX_VOLUME            = 2000.0;
	public static double MAX_RISK_PERCENT      = 0.3;
	public static double MAX_RISK_REWARD_RATIO = 3.0;
	public static double ORDER_LUFT            = 0.02;
	public static double MIN_POWER_RESERVE_TO_PRICE_RATIO = 0.005;
	public static double REALISTIC_POWER_RESERVE = 0.8;
	public static boolean SIMULATION_MODE = false;

	public static String DB_URL;
	public static String DB_USER;
	public static String DB_PASSWORD;

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
			MIN_POWER_RESERVE_TO_PRICE_RATIO = Double.parseDouble(configProps.getProperty("min_power_reserve_to_price_ratio", "0.005"));
			REALISTIC_POWER_RESERVE = Double.parseDouble(configProps.getProperty("realistic_power_reserve", "1"));
			SIMULATION_MODE = Boolean.parseBoolean(configProps.getProperty("simulation_mode", "false"));
			DB_URL = configProps.getProperty(dbKey, null);
			DB_USER = configProps.getProperty("db_user", null);
			DB_PASSWORD = configProps.getProperty("db_password", null);

			LiquibaseRunner.runLiquibase();

			setDefaultFont();

			JFrame frame = new JFrame();
			frame.setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 630);

			CustomTitleBar titleBar = new CustomTitleBar("Trade v. " + version + " (" + stage + ")", frame);

			Ibkr ibkr = new Ibkr();
			ibkr.run();

			Levels     levels     = new Levels(frame);
			Calculator calculator = new Calculator(frame, levels);
			AutoUpdate autoUpdate = new AutoUpdate(frame, ibkr);
			autoUpdate.addActivateListener(calculator::setAutoUpdate);

			OrderHelper    orderHelper    = new OrderHelper(ibkr);
			PositionHelper positionHelper = new PositionHelper(ibkr, autoUpdate);

			MainPanel mainPanel = new MainPanel(calculator, autoUpdate, levels, new Dimension(PREF_WIDTH, PREF_HEIGHT*2), new Dimension(0, 0), 5, FIELD_HEIGHT);

			JPanel mainContainer = new JPanel();
			mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

			mainContainer.add(mainPanel);

			frame.getContentPane().add(mainContainer);

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

			frame.add(titleBar, BorderLayout.NORTH);
			Border emptyBorder = BorderFactory.createEmptyBorder(RESIZE_EDGE, RESIZE_EDGE, RESIZE_EDGE, RESIZE_EDGE);

			frame.getRootPane().setBorder(emptyBorder);

			frame.setUndecorated(true);

			frame.pack();

			ResizeListener resizeListener = new ResizeListener(frame, RESIZE_EDGE);
			frame.addMouseListener(resizeListener);
			frame.addMouseMotionListener(resizeListener);

			frame.setVisible(true);

			log.info("max volume: {}", MAX_VOLUME);
			log.info("max risk/reward ratio: {}", MAX_RISK_REWARD_RATIO);
			log.info("max risk: {}%", MAX_RISK_PERCENT);
			log.info("order luft: {}", ORDER_LUFT);
			log.info("debug level: {}", DEBUG_LEVEL);

			log.info("Trade version {} ({}) is started", version, stage);

			if (!ibkr.isConnected()) {
				showWarningDlg(frame, "Not connected to IBKR. Auto update and orders will be simulated!");
				log.warn("Not connected to IBKR. Auto update and orders will be simulated!");
			}
		});
	}

	public static void setDefaultFont() {
		for (Object key : UIManager.getDefaults().keySet()) {
			if (UIManager.get(key) instanceof Font) {
				UIManager.put(key, new Font("Arial", Font.PLAIN, 14));
			}
		}
	}

	public static class CustomThemeListener implements ThemePreferenceListener {

		public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
			LafManager.installTheme(e.getPreferredThemeStyle());
		}
	}
}
