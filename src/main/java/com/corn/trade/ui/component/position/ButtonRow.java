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
package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;

import static com.corn.trade.util.Util.unbindEnterKey;

@SuppressWarnings("unused")
class ButtonRow extends JPanel {
	private final JButton buttonSellBE, buttonBE, button50, button25, button100;

	public ButtonRow(int fontSize, int padding, int hgap) {
		// Set the layout for evenly spaced buttons
		setLayout(new GridLayout(1, 5, hgap, 1)); // 1 row, 5 columns, 10px horizontal and vertical gaps
		setBorder(BorderFactory.createEmptyBorder(0, padding, padding, padding));

		// Initialize buttons
		buttonSellBE = new JButton("S_BE");
		buttonBE = new JButton("BE");
		button25 = new JButton("25");
		button50 = new JButton("50");
		button100 = new JButton("100");

		// Set the font size for the buttons
		buttonSellBE.setFont(new Font(buttonSellBE.getFont().getName(), Font.PLAIN, fontSize));
		buttonBE.setFont(new Font(buttonBE.getFont().getName(), Font.PLAIN, fontSize));
		button50.setFont(new Font(button50.getFont().getName(), Font.PLAIN, fontSize));
		button25.setFont(new Font(button25.getFont().getName(), Font.PLAIN, fontSize));
		button100.setFont(new Font(button100.getFont().getName(), Font.PLAIN, fontSize));

		// Add buttons to the panel
		add(buttonSellBE);
		add(buttonBE);
		add(button25);
		add(button50);
		add(button100);

		unbindEnterKey(buttonSellBE);
		unbindEnterKey(buttonBE);
		unbindEnterKey(button25);
		unbindEnterKey(button50);
		unbindEnterKey(button100);
	}

	// Getter methods if needed
	public JButton getButtonSellBE() {
		return buttonSellBE;
	}

	public JButton getButtonBE() {
		return buttonBE;
	}

	public JButton getButton50() {
		return button50;
	}

	public JButton getButton25() {
		return button25;
	}

	public JButton getButton100() {
		return button100;
	}

	public void setBeLabel(String beLabel) {
		if (buttonSellBE.isEnabled())
			buttonSellBE.setText(beLabel);
		else {
			buttonSellBE.setText("S_BE");
		}
	}
}
