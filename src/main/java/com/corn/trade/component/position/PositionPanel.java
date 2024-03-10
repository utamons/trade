package com.corn.trade.component.position;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PositionPanel extends JPanel {
	private final Map<String, Position> positions;
	private final JPanel contentPanel;
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

	public void addPosition(String label) {
		if (positions.isEmpty()) {
			remove(noPositionsLabel);
			add(contentPanel, BorderLayout.CENTER);
		}
		Position position = new Position();
		position.getInfoRow().getLabel().setText(label);
		positions.put(label, position);
		contentPanel.add(position);
		contentPanel.revalidate();
		contentPanel.repaint();
		updateDisplay();
	}

	public void removePosition(String label) {
		Position position = positions.remove(label);
		if (position != null) {
			contentPanel.remove(position);
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
