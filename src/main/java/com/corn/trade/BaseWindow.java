package com.corn.trade;

import com.corn.trade.component.CustomTitleBar;
import com.corn.trade.component.ResizeListener;
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

public abstract class BaseWindow {
	protected static final String version = "2.0";
	protected static final int RESIZE_EDGE = 4;
	protected static final  int    FIELD_HEIGHT = 40;
	public static        int    DEBUG_LEVEL  = 1;
	public static double MAX_VOLUME            = 2000.0;
	public static double MAX_RISK_PERCENT      = 0.3;
	public static double MAX_RISK_REWARD_RATIO = 3.0;
	public static double ORDER_LUFT            = 0.02;
	public static double MIN_POWER_RESERVE_TO_PRICE_RATIO = 0.005;
	public static double REALISTIC_POWER_RESERVE = 0.8;
	public static String DB_URL;
	public static String DB_USER;
	public static String DB_PASSWORD;

	public static boolean SIMULATION_MODE = false;
	protected static final  Theme  DARK_THEME   = new OneDarkTheme();
	protected static final  Theme  LIGHT_THEME  = new IntelliJTheme();
	protected static String dbKey = "db_url_dev";
	protected static String stage = null;
	protected              JFrame frame;

	protected static final Logger log = LoggerFactory.getLogger(BaseWindow.class);

	private String getTitle(String appName) {
		return appName + " v. " + version + " (" + stage + ")";
	}

	public BaseWindow(String[] args, String appName, Dimension size) {
		stage = args.length>0 && args[0].equals("-dev")? "dev" : "prod";
		dbKey = stage.equals("dev")? "db_url_dev" : "db_url_prod";

		loadProperties();

		String title = getTitle(appName);

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(size);
		frame.setUndecorated(true);

		// Set the custom title bar
		CustomTitleBar titleBar = new CustomTitleBar(title, frame);
		frame.add(titleBar, BorderLayout.NORTH);

		// Set resize listener
		ResizeListener resizeListener = new ResizeListener(frame, Trade.RESIZE_EDGE);
		frame.addMouseListener(resizeListener);
		frame.addMouseMotionListener(resizeListener);

		// Common appearance settings
		setupLookAndFeel();
		setupBorder();

		LiquibaseRunner.runLiquibase(DB_URL, DB_USER, DB_PASSWORD);
	}

	protected abstract void initializeComponents();

	private void setupLookAndFeel() {
		LafManager.enabledPreferenceChangeReporting(true);
		LafManager.setDecorationsEnabled(false);
		LafManager.addThemePreferenceChangeListener(new CustomThemeListener());
		LafManager.setThemeProvider(new DefaultThemeProvider(
				Trade.LIGHT_THEME,
				Trade.DARK_THEME,
				new HighContrastLightTheme(),
				new HighContrastDarkTheme()
		));

		setDefaultFont();
	}

	private void setupBorder() {
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		Border emptyBorder = BorderFactory.createEmptyBorder(Trade.RESIZE_EDGE, Trade.RESIZE_EDGE, Trade.RESIZE_EDGE, Trade.RESIZE_EDGE);
		frame.getRootPane().setBorder(BorderFactory.createCompoundBorder(border, emptyBorder));
	}

	private void loadProperties() {
		Properties configProps = new Properties();
		try (InputStream input = new FileInputStream("D:\\bin\\trade.properties")) {
			configProps.load(input);

			MAX_VOLUME = Integer.parseInt(configProps.getProperty("max_volume", "2000"));
			MAX_RISK_REWARD_RATIO =
					Double.parseDouble(configProps.getProperty("max_risk_reward_ratio", "3"));
			MAX_RISK_PERCENT = Double.parseDouble(configProps.getProperty("max_risk_percent", "0.5"));
			ORDER_LUFT = Double.parseDouble(configProps.getProperty("order_luft", "0.02"));
			DEBUG_LEVEL = Integer.parseInt(configProps.getProperty("debug_level", "2"));
			MIN_POWER_RESERVE_TO_PRICE_RATIO = Double.parseDouble(configProps.getProperty("min_power_reserve_to_price_ratio", "0.005"));
			REALISTIC_POWER_RESERVE = Double.parseDouble(configProps.getProperty("realistic_power_reserve", "1"));
			DB_URL = configProps.getProperty(dbKey, null);
			DB_USER = configProps.getProperty("db_user", null);
			DB_PASSWORD = configProps.getProperty("db_password", null);
			SIMULATION_MODE = Boolean.parseBoolean(configProps.getProperty("simulation_mode", "false"));

		} catch (IOException ex) {
			log.error("Error loading properties file: {}, {}", "D:\\bin\\trade.properties", ex.getMessage());
		}
	}

	public static class CustomThemeListener implements ThemePreferenceListener {
		public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
			LafManager.installTheme(e.getPreferredThemeStyle());
		}
	}

	public void display() {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	protected void setDefaultFont() {
		for (Object key : UIManager.getDefaults().keySet()) {
			if (UIManager.get(key) instanceof Font) {
				UIManager.put(key, new Font("Arial", Font.PLAIN, 14));
			}
		}
	}
}

