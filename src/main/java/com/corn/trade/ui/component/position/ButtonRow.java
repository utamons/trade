package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
class ButtonRow extends JPanel {
	private final JButton button25, button50, button75, button100;

	public ButtonRow(int fontSize, int padding, int hgap) {
		// Set the layout for evenly spaced buttons
		setLayout(new GridLayout(1, 5, hgap, 1)); // 1 row, 5 columns, 10px horizontal and vertical gaps
		setBorder(BorderFactory.createEmptyBorder(0, padding, padding, padding));

		// Initialize buttons
		button25 = new JButton("25");
		button50 = new JButton("50");
		button75 = new JButton("75");
		button100 = new JButton("100");

		// Set the font size for the buttons
		button25.setFont(new Font(button25.getFont().getName(), Font.PLAIN, fontSize));
		button50.setFont(new Font(button50.getFont().getName(), Font.PLAIN, fontSize));
		button75.setFont(new Font(button75.getFont().getName(), Font.PLAIN, fontSize));
		button100.setFont(new Font(button100.getFont().getName(), Font.PLAIN, fontSize));

		// Add buttons to the panel
		add(button25);
		add(button50);
		add(button75);
		add(button100);
	}

	// Getter methods if needed

	public JButton getButton25() {
		return button25;
	}

	public JButton getButton50() {
		return button50;
	}

	public JButton getButton75() {
		return button75;
	}

	public JButton getButton100() {
		return button100;
	}
}
