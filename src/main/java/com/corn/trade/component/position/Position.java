package com.corn.trade.component.position;

import javax.swing.*;

public class Position extends JPanel {

	private final InfoRow infoRow;

	public Position() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		infoRow = new InfoRow("AFRM", 12,5, 1, 2);
		ButtonRow buttonRow = new ButtonRow(12,5, 1);

		add(infoRow);
		add(buttonRow);
	}

	// Getter methods if needed
	public InfoRow getInfoRow() {
		return infoRow;
	}
}
