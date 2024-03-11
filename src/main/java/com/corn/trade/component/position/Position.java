package com.corn.trade.component.position;

import javax.swing.*;

public class Position extends JPanel {

	private final PosInfoRow posInfoRow;

	public Position() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		posInfoRow = new PosInfoRow("AFRM", 12, 5, 1, 2);
		ButtonRow buttonRow = new ButtonRow(12,5, 1);

		add(posInfoRow);
		add(buttonRow);
	}

	// Getter methods if needed
	public PosInfoRow getInfoRow() {
		return posInfoRow;
	}
}
