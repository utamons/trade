package com.corn.trade.component.panel;

import com.corn.trade.broker.ibkr.AutoUpdate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public abstract class BasePanel extends JPanel {

	protected AutoUpdate autoUpdate;
	public BasePanel(AutoUpdate autoUpdate,  Dimension preferredSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		this.setBorder(emptyBorder);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
		this.autoUpdate = autoUpdate;
	}
}
