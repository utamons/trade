/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade;

import com.corn.trade.type.Stage;
import com.corn.trade.ui.component.CustomTitleBar;
import com.corn.trade.ui.component.ResizeListener;
import com.corn.trade.util.LiquibaseRunner;
import com.corn.trade.util.LoggerSetup;
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
	protected static final String  version               = "2.0.26";
	protected static final int     RESIZE_EDGE           = 4;
	protected static final int     FIELD_HEIGHT          = 40;
	protected static final Theme   DARK_THEME            = new OneDarkTheme();
	protected static final Theme   LIGHT_THEME           = new IntelliJTheme();
	protected static final Logger  log                   = LoggerFactory.getLogger(BaseWindow.class);
	public static          int     DEBUG_LEVEL           = 1;
	public static          double  MAX_VOLUME            = 2000.0;
	public static          double  MAX_RISK_PERCENT      = 0.3;
	public static          double  MAX_RISK_REWARD_RATIO = 3.0;
	public static          double  ORDER_LUFT            = 0.02;
	public static          String  DB_URL;
	public static          String  DB_USER;
	public static          String  DB_PASSWORD;
	public static          Stage   STAGE;
	public static          boolean SIMULATION_MODE       = false;
	protected static       String  dbKey                 = "db_url_dev";
	public static          long    MAX_TRADES_PER_DAY    = 3;
	public static          double  MAX_DAILY_LOSS        = -30.0;
	public static          double  MAX_WEEKLY_LOSS       = -90.0;
	public static          double  MAX_MONTHLY_LOSS      = -300.0;
	public static          double  DEFAULT_STOP_LOSS_PERCENTAGE = 4;
	protected final        JFrame  frame;

	public BaseWindow(String[] args, String appName, Dimension size) {
		STAGE = args.length > 0 && args[0].equals("-dev") ? Stage.DEV : Stage.PROD;
		loadProperties();

		LoggerSetup.configureLogging(STAGE);

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
		ResizeListener resizeListener = new ResizeListener(frame, TradeWindow.RESIZE_EDGE);
		frame.addMouseListener(resizeListener);
		frame.addMouseMotionListener(resizeListener);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Common appearance settings
		setupLookAndFeel();
		setupBorder();

		LiquibaseRunner.runLiquibase(DB_URL, DB_USER, DB_PASSWORD);

		initializeComponents();
	}

	public static void loadProperties() {
		Properties configProps = new Properties();
		try (InputStream input = new FileInputStream("D:\\bin\\trade.properties")) {
			configProps.load(input);
			SIMULATION_MODE = Boolean.parseBoolean(configProps.getProperty("simulation_mode", "false"));
			if (SIMULATION_MODE){
				STAGE = Stage.SIMULATION;
			}

			dbKey = STAGE == Stage.PROD ? "db_url_prod" : "db_url_dev";

			MAX_VOLUME = Integer.parseInt(configProps.getProperty("max_volume", "2000"));
			MAX_RISK_REWARD_RATIO =
					Double.parseDouble(configProps.getProperty("max_risk_reward_ratio", "3"));
			MAX_RISK_PERCENT = Double.parseDouble(configProps.getProperty("max_risk_percent", "0.5"));
			ORDER_LUFT = Double.parseDouble(configProps.getProperty("order_luft", "0.02"));
			DEBUG_LEVEL = Integer.parseInt(configProps.getProperty("debug_level", "2"));
			DB_URL = configProps.getProperty(dbKey, null);
			DB_USER = configProps.getProperty("db_user", null);
			DB_PASSWORD = configProps.getProperty("db_password", null);

			MAX_TRADES_PER_DAY = Long.parseLong(configProps.getProperty("max_trades_per_day", "3"));
			MAX_DAILY_LOSS = Double.parseDouble(configProps.getProperty("max_daily_loss", "-30.0"));
			MAX_WEEKLY_LOSS = Double.parseDouble(configProps.getProperty("max_weekly_loss", "-90.0"));
			MAX_MONTHLY_LOSS = Double.parseDouble(configProps.getProperty("max_monthly_loss", "-300.0"));
			DEFAULT_STOP_LOSS_PERCENTAGE = Double.parseDouble(configProps.getProperty("default_stop_loss_percentage", "4"));

		} catch (IOException ex) {
			log.error("Error loading properties file: {}, {}", "D:\\bin\\trade.properties", ex.getMessage());
		}
	}

	private String getTitle(String appName) {
		return appName + " v. " + version + " (" + STAGE + ")";
	}

	protected abstract void initializeComponents();

	private void setupLookAndFeel() {
		LafManager.enabledPreferenceChangeReporting(true);
		LafManager.setDecorationsEnabled(false);
		LafManager.addThemePreferenceChangeListener(new CustomThemeListener());
		LafManager.setThemeProvider(new DefaultThemeProvider(
				TradeWindow.LIGHT_THEME,
				TradeWindow.DARK_THEME,
				new HighContrastLightTheme(),
				new HighContrastDarkTheme()
		));

		setDefaultFont();
	}

	private void setupBorder() {
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		Border emptyBorder = BorderFactory.createEmptyBorder(TradeWindow.RESIZE_EDGE,
		                                                     TradeWindow.RESIZE_EDGE,
		                                                     TradeWindow.RESIZE_EDGE,
		                                                     TradeWindow.RESIZE_EDGE);
		frame.getRootPane().setBorder(BorderFactory.createCompoundBorder(border, emptyBorder));
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

	public static class CustomThemeListener implements ThemePreferenceListener {
		public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
			LafManager.installTheme(e.getPreferredThemeStyle());
		}
	}
}

