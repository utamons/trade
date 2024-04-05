package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;

public class PositionRow extends JPanel {

	private final PosInfoRow posInfoRow;
	private final ButtonRow buttonRow;

	public PositionRow() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		posInfoRow = new PosInfoRow("AFRM", 12, 5, 1, 2);
		buttonRow = new ButtonRow(12, 5, 1);

		add(posInfoRow);
		add(buttonRow);
	}

	public void setLabel(String label) {
		posInfoRow.getLabel().setText(label);
	}

	public void setQtt(long qtt) {
		posInfoRow.setQtt(qtt);
	}

	public void setSl(Double sl) {
		posInfoRow.setSl(sl);
	}

	public void setBe(Double be) {
		posInfoRow.setBe(be);
	}

	public void setPs(Double ps) {
		posInfoRow.setPs(ps);
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

	public JButton getFixButton() {
		return buttonRow.getFixButton();
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

	public JButton getAllButton() {
		return buttonRow.getAllButton();
	}
}
