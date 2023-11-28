package com.corn.trade.desktop.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LabeledComboBox extends JPanel {

	private final JComboBox<String> comboBox;

	// Constructor
	public LabeledComboBox(String labelText, String[] items) {
		// Initialize label and combo box
		JLabel label = new JLabel(labelText);
		comboBox = new JComboBox<>(items);

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(comboBox, BorderLayout.EAST);
	}

	// Method to add a change listener to the combo box
	public void addChangeListener(ActionListener listener) {
		comboBox.addActionListener(listener);
	}

	// Getter for the selected item
	public String getSelectedItem() {
		return (String) comboBox.getSelectedItem();
	}

	// Setter for the selected item
	public void setSelectedItem(String item) {
		comboBox.setSelectedItem(item);
	}
}

