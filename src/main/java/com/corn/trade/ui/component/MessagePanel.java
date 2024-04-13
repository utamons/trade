package com.corn.trade.ui.component;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JPanel {

	private final JLabel  msg;
	private final Timer   delayTimer;
	private       boolean delay = false;

	public MessagePanel(int paddingTop, int paddingBottom) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(paddingTop, 5, paddingBottom, 5));

		msg = new JLabel();

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		jPanel.add(msg);

		add(jPanel, BorderLayout.CENTER);
		delayTimer = new Timer(5000, e -> delay = false);
	}

	public void info(String message) {
		if (!delay) {
			show(message, Color.GREEN.darker());
		}
	}

	private void show(String message, Color color) {
		msg.setText(message);
		msg.setForeground(color);
	}

	public void error(String message) {
		show(message, Color.RED.darker());
		if (!delay) {
			delay = true;
			delayTimer.start();
		} else
			delayTimer.restart();
	}

	public void success(String message) {
		if (!delay) {
			show(message, Color.GREEN.darker());
			delay = true;
			delayTimer.start();
		}
	}

	public void warning(String message) {
		if (!delay) {
			show(message, Color.ORANGE);
			delay = true;
			delayTimer.start();
		}
	}

	public void clear() {
		msg.setText("");
	}
}
