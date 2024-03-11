package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class RowPanel extends JPanel {

	private final JPanel jPanel;

	public RowPanel(int vGap) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(vGap, 0, vGap, 0));

		jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		add(jPanel, BorderLayout.CENTER);
	}

	@Override
	public Component add(Component comp) {
		jPanel.add(comp);
		return comp;
	}
}

