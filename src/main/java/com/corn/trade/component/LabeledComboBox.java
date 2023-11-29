package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("unused")
public class LabeledComboBox extends JPanel {

	private final JComboBox<String> comboBox;

	// Constructor
	public LabeledComboBox(String labelText, String[] items, int padding, int height) {
		// Initialize label and combo box
		JLabel label = new JLabel(labelText);
		comboBox = new JComboBox<>(items);
		this.setMaximumSize(new Dimension(5000,height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(comboBox, BorderLayout.EAST);
	}

	public void addChangeListener(ActionListener listener) {
		comboBox.addActionListener(listener);
	}

	public String getSelectedItem() {
		return (String) comboBox.getSelectedItem();
	}

	public void setSelectedItem(String item) {
		comboBox.setSelectedItem(item);
	}
}

