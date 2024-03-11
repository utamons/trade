package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JPanel {

	private final JLabel msg;

	public MessagePanel(int paddingTop, int paddingBottom) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(paddingTop, 5, paddingBottom, 5));

		msg = new JLabel();

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		jPanel.add(msg);

		add(jPanel, BorderLayout.CENTER);
	}

	public void show(String message) {
		msg.setText(message);
	}
}