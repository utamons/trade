package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class RowPanel extends JPanel {

	private final JPanel buttonPanel;

	public RowPanel() {
		setLayout(new BorderLayout());

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		add(buttonPanel, BorderLayout.CENTER);
	}

	@Override
	public Component add(Component comp) {
		if (comp instanceof JButton || comp instanceof JCheckBox) {
			buttonPanel.add(comp);
		}
		return comp;
	}
}

