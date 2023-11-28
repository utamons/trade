package com.corn.trade.desktop;

import com.corn.trade.desktop.panel.InputPanel;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider;

import javax.swing.*;
import java.awt.*;

public class App {

	public static final Theme DARK_THEME = new DarculaTheme();
	public static final Theme LIGHT_THEME = new IntelliJTheme();

	static class CustomThemeListener implements ThemePreferenceListener {

		public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
			LafManager.installTheme(e.getPreferredThemeStyle());
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			setDefaultFont();

			JFrame frame = new JFrame("Trade Calculator");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500, 500);

			InputPanel inputPanel1 = new InputPanel();

			frame.getContentPane().setLayout(new GridLayout(2, 2, 1, 1));
			frame.getContentPane().add(inputPanel1);
			frame.getContentPane().add(new InputPanel());
			frame.getContentPane().add(new InputPanel());
			frame.getContentPane().add(new InputPanel());

			frame.setLocationRelativeTo(null);
			frame.pack();

			LafManager.enabledPreferenceChangeReporting(true);

			LafManager.addThemePreferenceChangeListener(new CustomThemeListener());

			LafManager.setThemeProvider(new DefaultThemeProvider(
					LIGHT_THEME,
					DARK_THEME,
					new HighContrastLightTheme(),
					new HighContrastDarkTheme()
			));

			frame.setVisible(true);
		});
	}

	private static void setDefaultFont() {
		UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
		UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 14));
		// Add other components here

		// For a comprehensive approach, iterate over all keys
		for (Object key : UIManager.getDefaults().keySet()) {
			if (UIManager.get(key) instanceof Font) {
				UIManager.put(key, new Font("Arial", Font.PLAIN, 14));
			}
		}
	}
}
