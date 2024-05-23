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

public class PositionRow extends JPanel {

	private final PosInfoRow posInfoRow;
	private final ButtonRow buttonRow;

	public PositionRow() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		posInfoRow = new PosInfoRow("AFRM", 12, 5, 3, 3);
		buttonRow = new ButtonRow(12, 5, 1);

		add(posInfoRow);
		add(buttonRow);
	}

	public void setLabel(String label) {
		posInfoRow.getLabel().setText(label);
	}

	public void setBe(Double be) {
		posInfoRow.setBe(be);
	}

	public void setQtt(String qtt) {
		posInfoRow.setQtt(qtt);
	}

	public void setSl(Double sl) {
		posInfoRow.setSl(sl);
	}

	public void setGoal(Double goal) {
		posInfoRow.setGoal(goal);
	}

	public void setDst(Double dst) {
		posInfoRow.setDst(dst);
	}

	public void setPl(Double pl) {
		posInfoRow.setPl(pl);
	}

	public void setPsColor(Color color) {
		posInfoRow.setPsColor(color);
	}

	public void setPlColor(Color color) {
		posInfoRow.setPlColor(color);
	}

	public JButton getButtonBE() {
		return buttonRow.getButtonBE();
	}

	public JButton getButton25() {
		return buttonRow.getButton25();
	}

	public JButton getButton50() {
		return buttonRow.getButton50();
	}

	public JButton getButton75() {
		return buttonRow.getButton75();
	}

	public JButton getButton100() {
		return buttonRow.getButton100();
	}
}
