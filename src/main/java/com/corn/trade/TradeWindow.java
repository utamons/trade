package com.corn.trade;

import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.ui.view.TradePanel;
import com.corn.trade.ui.controller.TradeController;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class TradeWindow extends BaseWindow {
	private static final  int    PREF_HEIGHT  = 850;
	private static final  int    PREF_WIDTH   = 450;
	private static final Logger log          = LoggerFactory.getLogger(TradeWindow.class);

	public TradeWindow(String[] args) {
		super(args, "Trade", new Dimension(PREF_WIDTH, PREF_HEIGHT));
	}

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> log.error("Uncaught exception in thread '{}'", thread.getName(), throwable));

		SwingUtilities.invokeLater(() -> {
			TradeWindow tradeWindow = new TradeWindow(args);
			tradeWindow.display();
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Application is closing, releasing JPA resources...");
			JpaUtil.close(); // Assuming JPAUtil.close() is a static method to close EntityManagerFactory
		}));
	}

	@Override
	protected void initializeComponents() {
		TradePanel tradePanel;
		try {
			TradeController tradeController = new TradeController();
			tradePanel = new TradePanel(tradeController, new Dimension(PREF_WIDTH, PREF_HEIGHT), new Dimension(0, 0), 5, FIELD_HEIGHT);
		} catch (Exception e) {
			Util.showErrorDlg(frame, "Error initializing TradePanel - " + e.getMessage(), true);
			throw new RuntimeException(e);
		}

		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

		mainContainer.add(tradePanel);

		frame.getContentPane().add(mainContainer);

		log.info("max volume: {}", MAX_VOLUME);
		log.info("max risk/reward ratio: {}", MAX_RISK_REWARD_RATIO);
		log.info("max risk: {}%", MAX_RISK_PERCENT);
		log.info("order luft: {}", ORDER_LUFT);
		log.info("debug level: {}", DEBUG_LEVEL);

		log.info("Trade version {} ({}) is started", version, STAGE);
	}
}
