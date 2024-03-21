package com.corn.trade.component.position;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("unused")
class ButtonRow extends JPanel {
	private final JButton fixButton, button25, button50, button75, allButton;

	public ButtonRow(int fontSize, int padding, int hgap) {
		// Set the layout for evenly spaced buttons
		setLayout(new GridLayout(1, 5, hgap, 1)); // 1 row, 5 columns, 10px horizontal and vertical gaps
		setBorder(BorderFactory.createEmptyBorder(0, padding, padding, padding));
		//setBackground(Color.LIGHT_GRAY);

		// Initialize buttons
		fixButton = new JButton("Fix");
		button25 = new JButton("25");
		button50 = new JButton("50");
		button75 = new JButton("75");
		allButton = new JButton("All");

		// Set the font size for the buttons
		fixButton.setFont(new Font(fixButton.getFont().getName(), Font.PLAIN, fontSize));
		button25.setFont(new Font(button25.getFont().getName(), Font.PLAIN, fontSize));
		button50.setFont(new Font(button50.getFont().getName(), Font.PLAIN, fontSize));
		button75.setFont(new Font(button75.getFont().getName(), Font.PLAIN, fontSize));
		allButton.setFont(new Font(allButton.getFont().getName(), Font.PLAIN, fontSize));

		// Add buttons to the panel
		add(fixButton);
		add(button25);
		add(button50);
		add(button75);
		add(allButton);
	}

	// Getter methods if needed
	public JButton getFixButton() {
		return fixButton;
	}

	public JButton getButton25() {
		return button25;
	}

	public JButton getButton50() {
		return button50;
	}

	public JButton getButton75() {
		return button75;
	}

	public JButton getAllButton() {
		return allButton;
	}
}
