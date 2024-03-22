package com.corn.trade.ui.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public abstract class BasePanel extends JPanel {
	public BasePanel(Dimension preferredSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		this.setBorder(emptyBorder);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
	}
}
