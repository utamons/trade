/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.ui.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;
import java.util.List;

@SuppressWarnings("unused")
public class LabeledComboBox extends JPanel {

	private final JComboBox<String> comboBox;

	public LabeledComboBox(String labelText, List<String> items, int padding, int height) {
		this(labelText, items, padding, height, null);
	}

	// Constructor
	public LabeledComboBox(String labelText, List<String> items, int padding, int height, Consumer<String> consumer ) {
		// Initialize label and combo box
		JLabel label = new JLabel(labelText);
		String[] itemsArray = items.toArray(new String[0]);
		comboBox = new JComboBox<>(itemsArray);
		this.setMaximumSize(new Dimension(5000, height));
		this.setMinimumSize(new Dimension(500,height));
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
	public void setEnabled(boolean enabled) {
		comboBox.setEnabled(enabled);
	}
}

