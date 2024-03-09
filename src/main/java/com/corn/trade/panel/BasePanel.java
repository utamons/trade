package com.corn.trade.panel;

import com.corn.trade.ibkr.AutoUpdate;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {

	protected Calculator calculator;
	protected AutoUpdate autoUpdate;
	protected Levels     levels;

	@Deprecated
	public BasePanel(String title, Calculator calculator, AutoUpdate autoUpdate, Levels levels, Dimension preferredSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		this.setBorder(emptyBorder);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
		this.calculator = calculator;
		this.autoUpdate = autoUpdate;
		this.levels = levels;
	}

	public BasePanel(Calculator calculator, AutoUpdate autoUpdate, Levels levels, Dimension preferredSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		this.setBorder(emptyBorder);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
		this.calculator = calculator;
		this.autoUpdate = autoUpdate;
		this.levels = levels;
	}
}
