package com.corn.trade;

import com.corn.trade.entity.MyEntity;
import com.corn.trade.ibkr.Ibkr;
import com.corn.trade.panel.*;
import com.corn.trade.trade.Calculator;
import com.corn.trade.util.Util;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.*;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.info.DefaultThemeProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import javax.swing.*;
import java.awt.*;

import static com.corn.trade.util.Util.log;

public class App {

	public static final  Theme DARK_THEME   = new OneDarkTheme();
	public static final  Theme LIGHT_THEME  = new IntelliJTheme();
	private static final int   FIELD_HEIGHT = 40;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			log("Calculator version 1.0.0");
			setDefaultFont();

			JFrame frame = new JFrame("Trade Calculator");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 630);

			Calculator calculator = new Calculator(frame);

			InputPanel inputPanel = new InputPanel(
					calculator,
					new Dimension(500, 300),
					new Dimension(500, 300),
					5, FIELD_HEIGHT);
			PowerPanel powerPanel = new PowerPanel(
					calculator,
					new Dimension(300, 300),
					new Dimension(300, 300),
					5, FIELD_HEIGHT);
			TradePanel tradePanel = new TradePanel(
					calculator,
					new Dimension(300, 300),
					new Dimension(300, 100),
					5, FIELD_HEIGHT);
			RiskPanel riskPanel = new RiskPanel(
					calculator,
					new Dimension(300, 300),
					new Dimension(300, 300),
					5, FIELD_HEIGHT);
			OrderPanel orderPanel = new OrderPanel(
					calculator,
					new Dimension(300, 300),
					new Dimension(300, 100),
					5, FIELD_HEIGHT);

			frame.getContentPane().setLayout(new GridLayout(1, 2));

			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new GridLayout(2, 1));
			leftPanel.add(inputPanel);
			leftPanel.add(tradePanel);

			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new GridLayout(3, 1));
			rightPanel.add(powerPanel);
			rightPanel.add(riskPanel);
			rightPanel.add(orderPanel);

			frame.getContentPane().add(leftPanel);
			frame.getContentPane().add(rightPanel);

			frame.setLocationRelativeTo(null);

			LafManager.enabledPreferenceChangeReporting(true);

			LafManager.addThemePreferenceChangeListener(new CustomThemeListener());

			LafManager.setThemeProvider(new DefaultThemeProvider(
					LIGHT_THEME,
					DARK_THEME,
					new HighContrastLightTheme(),
					new HighContrastDarkTheme()
			));

			frame.setVisible(true);
			log("Application started");
		});
		//Ibkr ibkr = new Ibkr();
		//ibkr.run();
		Util.log("START");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("TradePersistenceUnit");

		EntityManager em = emf.createEntityManager();

	/*	transaction.begin();

		MyEntity entity = new MyEntity();
		entity.setName("Test");
		Util.log("CREATING");
		em.persist(entity);
		Util.log("PERSISTING");

		transaction.commit();

		Util.log("ID: {}", entity.getId());*/

		java.util.List<MyEntity> entities = em.createQuery("SELECT e FROM MyEntity e", MyEntity.class).getResultList();

		entities.forEach(
				e->log("id: {}", e.getId())
		);

		em.close();
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
