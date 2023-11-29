package com.corn.trade.panel;

import com.corn.trade.trade.Calculator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {

	protected Calculator calculator;

	public BasePanel(String title, Calculator calculator, Dimension maxSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border       lineBorder   = BorderFactory.createLineBorder(Color.LIGHT_GRAY); // Padding
		TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, title);
		titledBorder.setTitleFont(new Font("Arial", Font.PLAIN, 12));
		titledBorder.setTitleColor(Color.GRAY);
		Border compoundBorder = BorderFactory.createCompoundBorder(emptyBorder, titledBorder);

		this.setBorder(BorderFactory.createCompoundBorder(compoundBorder, emptyBorder));
		this.setMaximumSize(maxSize);
		this.setPreferredSize(maxSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
		this.calculator = calculator;
	}
}
