package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LabeledComboBox extends JPanel {

	private final JComboBox<String> comboBox;

	// Constructor
	public LabeledComboBox(String labelText, String[] items, int padding, int height, Consumer<String> consumer ) {
		// Initialize label and combo box
		JLabel label = new JLabel(labelText);
		comboBox = new JComboBox<>(items);
		this.setMaximumSize(new Dimension(5000,height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);

		comboBox.addActionListener(e -> {
			if (consumer != null) {
				consumer.accept((String) comboBox.getSelectedItem());
			}
		});

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(comboBox, BorderLayout.EAST);
	}

	public String getSelectedItem() {
		return (String) comboBox.getSelectedItem();
	}

	public void setSelectedItem(String item) {
		comboBox.setSelectedItem(item);
	}
}

