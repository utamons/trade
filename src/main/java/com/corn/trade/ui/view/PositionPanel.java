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
package com.corn.trade.ui.view;

import com.corn.trade.ui.component.position.PositionRow;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PositionPanel extends JPanel implements PositionView {
	private final Map<String, PositionRow> positions;
	private final JPanel                   contentPanel;
	private final JLabel noPositionsLabel;

	public PositionPanel() {
		positions = new HashMap<>();
		setLayout(new BorderLayout());
		Border       emptyBorder  = BorderFactory.createEmptyBorder(0, 5, 5, 5);
		Border       lineBorder   = BorderFactory.createLineBorder(Color.LIGHT_GRAY); // Padding
		TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "Positions");
		titledBorder.setTitleFont(new Font("Arial", Font.PLAIN, 12));
		titledBorder.setTitleColor(Color.GRAY);
		Border compoundBorder = BorderFactory.createCompoundBorder(emptyBorder, titledBorder);

		this.setBorder(BorderFactory.createCompoundBorder(compoundBorder, emptyBorder));

		noPositionsLabel = new JLabel("none");
		noPositionsLabel.setFont(new Font(noPositionsLabel.getFont().getName(), Font.PLAIN, 12));
		noPositionsLabel.setHorizontalAlignment(JLabel.CENTER);

		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		add(noPositionsLabel, BorderLayout.CENTER);

		updateDisplay();
	}

	@Override
	public PositionRow addPosition(String label) {
		if (positions.isEmpty()) {
			remove(noPositionsLabel);
			add(contentPanel, BorderLayout.CENTER);
		}
		PositionRow positionRow = new PositionRow();
		positionRow.setLabel(label);
		positions.put(label, positionRow);
		contentPanel.add(positionRow);
		contentPanel.revalidate();
		contentPanel.repaint();
		updateDisplay();
		return positionRow;
	}

	@Override
	public void removePosition(String label) {
		PositionRow positionRow = positions.remove(label);
		if (positionRow != null) {
			contentPanel.remove(positionRow);
			contentPanel.revalidate();
			contentPanel.repaint();
		}
		if (positions.isEmpty()) {
			add(noPositionsLabel, BorderLayout.CENTER);
			remove(contentPanel);
		}
		updateDisplay();
	}

	private void updateDisplay() {
		if (positions.isEmpty()) {
			noPositionsLabel.setVisible(true);
			contentPanel.setVisible(false);
		} else {
			noPositionsLabel.setVisible(false);
			contentPanel.setVisible(true);
		}
		revalidate();
		repaint();
	}
}
