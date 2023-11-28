package com.corn.trade.desktop.component;

import javax.swing.*;
import java.awt.*;

public class ButtonRowPanel extends JPanel {

	private final JPanel buttonPanel;

	public ButtonRowPanel() {
		// Use BorderLayout to center the button panel
		setLayout(new BorderLayout());

		// Panel to hold buttons
		buttonPanel = new JPanel();
		// FlowLayout with center alignment and equal spacing
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// Add the button panel to the center of this panel
		add(buttonPanel, BorderLayout.CENTER);
	}

	@Override
	public Component add(Component comp) {
		// Only add buttons to the button panel
		if (comp instanceof JButton) {
			buttonPanel.add(comp);
		}
		return comp;
	}
}

