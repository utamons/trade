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

import static com.corn.trade.util.Util.showWarningDlg;

public class App {
	private static final Logger log = LoggerFactory.getLogger(App.class);
	public static final  Theme DARK_THEME   = new OneDarkTheme();
	public static final  Theme LIGHT_THEME  = new IntelliJTheme();
	private static final int   FIELD_HEIGHT = 40;
	public static final int PREF_HEIGHT = 250;
	public static final int PREF_WIDTH = 330;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			log.info("Calculator version 1.0.0");
			setDefaultFont();

			JFrame frame = new JFrame("Trade Calculator v. 1.0");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 630);

			Ibkr ibkr = new Ibkr();
			ibkr.run();

			Levels levels = new Levels(frame);
			Calculator calculator = new Calculator(frame, levels);
			AutoUpdate autoUpdate = new AutoUpdate(frame, ibkr);
			autoUpdate.addActivateListener(calculator::setAutoUpdate);

			OrderHelper orderHelper = new OrderHelper(ibkr);
			PositionHelper positionHelper = new PositionHelper(ibkr);

			InputPanel inputPanel = new InputPanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					5, FIELD_HEIGHT);
			PowerPanel powerPanel = new PowerPanel(
					calculator,
					autoUpdate,
					levels,
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
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
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
					new Dimension(PREF_WIDTH, PREF_HEIGHT),
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

			frame.getContentPane().setLayout(new GridLayout(1, 3));

			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new GridLayout(1, 1));
			leftPanel.add(inputPanel);

			JPanel rightPanel1 = new JPanel();
			rightPanel1.setLayout(new GridLayout(2, 1));
			rightPanel1.add(tradePanel);
			rightPanel1.add(powerPanel);

			JPanel rightPanel2 = new JPanel();
			rightPanel2.setLayout(new GridLayout(2, 1));
			rightPanel2.add(riskPanel);
			rightPanel2.add(orderPanel);

			frame.getContentPane().add(leftPanel);
			frame.getContentPane().add(rightPanel1);
			frame.getContentPane().add(rightPanel2);

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
