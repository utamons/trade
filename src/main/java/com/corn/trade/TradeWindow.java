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

import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.jpa.TradeRepo;
import com.corn.trade.type.TradeStatus;
import com.corn.trade.ui.view.TradePanel;
import com.corn.trade.ui.controller.TradeController;
import com.corn.trade.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TradeWindow extends BaseWindow {
	private static final  int    PREF_HEIGHT  = 850;
	private static final  int    PREF_WIDTH   = 450;
	private static final Logger    log       = LoggerFactory.getLogger(TradeWindow.class);
	private final        TradeRepo tradeRepo = new TradeRepo();

	public TradeWindow(String[] args) {
		super(args, "Trade", new Dimension(PREF_WIDTH, PREF_HEIGHT));
	}

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> log.error("Uncaught exception in thread '{}'", thread.getName(), throwable));

		SwingUtilities.invokeLater(() -> {
			TradeWindow tradeWindow = new TradeWindow(args);
			tradeWindow.display();
			tradeWindow.checkOpenTrades();
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("Application is closing, releasing JPA resources...");
			JpaUtil.close(); // Assuming JPAUtil.close() is a static method to close EntityManagerFactory
		}));
	}

	private void checkOpenTrades() {
		java.util.List<Trade> openTrades = tradeRepo.findAllOpen();

		if (!openTrades.isEmpty()) {
			showOpenTradesWarning(openTrades);
		}
	}

	private void showOpenTradesWarning(java.util.List<Trade> openTrades) {
		String message = "You have " + openTrades.size() + " trade(s) open:";
		JDialog dialog = new JDialog(this.frame, "Warning", true);
		dialog.setLayout(new BorderLayout());

		// Create a main panel with padding
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setLayout(new BorderLayout(10, 10));

		JLabel messageLabel = new JLabel(message);
		mainPanel.add(messageLabel, BorderLayout.NORTH);

		List<String> assetInfo = openTrades.stream()
		                         .map(trade -> trade.getAsset().getName() + " - " + trade.getCreatedAt())
		                         .toList();

		String assetListHtml = "<html>" + String.join("<br>", assetInfo) + "</html>";
		JLabel assetListLabel = new JLabel(assetListHtml);
		JScrollPane scrollPane = new JScrollPane(assetListLabel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton closeAllButton = new JButton("Close all");
		JButton cancelButton = new JButton("Cancel");

		closeAllButton.addActionListener(e -> {
			openTrades.forEach(trade -> {
				trade.setStatus(TradeStatus.CLOSED.name());
				tradeRepo.update(trade);
			});
			dialog.dispose();
		});

		cancelButton.addActionListener(e -> dialog.dispose());

		buttonPanel.add(closeAllButton);
		buttonPanel.add(cancelButton);

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		dialog.add(mainPanel, BorderLayout.CENTER);
		dialog.setSize(300, 200);
		dialog.setLocationRelativeTo(this.frame);
		dialog.setVisible(true);
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
