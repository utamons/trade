package com.corn.trade;

import com.corn.trade.broker.ibkr.AutoUpdate;
import com.corn.trade.broker.ibkr.Ibkr;
import com.corn.trade.component.panel.MainPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

import static com.corn.trade.util.Util.showWarningDlg;

public class TradeWindow extends BaseWindow {
	private static final  int    PREF_HEIGHT  = 400;
	private static final  int    PREF_WIDTH   = 330;
	private static final Logger log          = LoggerFactory.getLogger(TradeWindow.class);

	public TradeWindow(String[] args) {
		super(args, "Trade", new Dimension(350, 800));
		initializeComponents();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			TradeWindow tradeWindow = new TradeWindow(args);
			tradeWindow.display();
		});
	}

	@Override
	protected void initializeComponents() {
		Ibkr ibkr = new Ibkr();
		ibkr.run();

		AutoUpdate autoUpdate = new AutoUpdate(frame, ibkr);

		MainPanel mainPanel = new MainPanel(autoUpdate, new Dimension(PREF_WIDTH, PREF_HEIGHT*2), new Dimension(0, 0), 5, FIELD_HEIGHT);

		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

		mainContainer.add(mainPanel);

		frame.getContentPane().add(mainContainer);

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
	}
}
