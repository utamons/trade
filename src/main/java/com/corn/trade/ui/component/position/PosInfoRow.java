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
import com.corn.trade.ui.component.InfoField;

import static com.corn.trade.BaseWindow.MAX_RISK_REWARD_RATIO;
import static com.corn.trade.util.Util.fmt;

public class PosInfoRow extends JPanel {
	private final InfoField qttField, slField, beField, targetField, rrField, plField;
	private final JLabel label;

	public PosInfoRow(String labelText, int fontSize, int padding, int hgap, int hSpacing) {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		setPreferredSize(new Dimension(150, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 0, 0, hgap);

		label = new JLabel(labelText);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, fontSize));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hSpacing, 0));
		labelPanel.add(label);


		beField = new InfoField("BE", fontSize, 0, hSpacing, 30);
		qttField = new InfoField("Qtt:", fontSize, 0, hSpacing, 30);
		slField = new InfoField("Sl:", fontSize, 0, hSpacing, 30);
		targetField = new InfoField("Tg:", fontSize, 0, hSpacing, 30);
		rrField = new InfoField("R/R:", fontSize, 0, hSpacing, 30);
		plField = new InfoField("P/L:", fontSize, 0, hSpacing, 30);

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		add(labelPanel, gbc);

		gbc.gridx = 1;
		add(beField, gbc);
		gbc.gridx = 2;
		add(qttField, gbc);
		gbc.gridx = 3;
		add(slField, gbc);
		gbc.gridx = 4;
		add(targetField, gbc);
		gbc.gridx = 5;
		add(rrField, gbc);
		gbc.gridx = 6;
		add(plField, gbc);
	}

	public JLabel getLabel() {
		return label;
	}

	public void setBe(Double be) {
		beField.setInfoText(fmt(be));
	}

	public void setQtt(String qtt) {
		qttField.setInfoText(qtt);
	}

	public void setSl(Double sl) {
		slField.setInfoText(fmt(sl));
	}

	public void setTarget(Double target) {
		targetField.setInfoText(fmt(target));
	}

	public void setRr(Double rr) {
		rrField.setInfoText(fmt(rr) + "/1");
		if (rr <= 0)
			rrField.setInfoFieldColor(Color.RED.darker());
		else if (rr > 0 && rr <= 1)
			rrField.setInfoFieldColor(Color.ORANGE);
		else if (rr > 1 && rr <= (1/MAX_RISK_REWARD_RATIO))
			rrField.setInfoFieldColor(Color.CYAN.darker());
		else
			rrField.setInfoFieldColor(Color.GREEN.darker());
	}

	public void setPl(Double pl) {
		plField.setInfoText(fmt(pl));
	}

	public void setPlColor(Color color) {
		plField.setInfoFieldColor(color);
	}
}
