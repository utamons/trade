package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class ColoredCircleIcon implements Icon {
	private final Color color;
	private final int     diameter;
	private final boolean on;

	public ColoredCircleIcon(boolean on, Color color, int diameter) {
		this.on = on;
		this.color = color;
		this.diameter = diameter;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (on) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setColor(color);
			g2d.fillOval(x, y, diameter, diameter);
			g2d.dispose();
		}
	}

	@Override
	public int getIconWidth() {
		return on ? diameter : 0;
	}

	@Override
	public int getIconHeight() {
		return on ? diameter : 0;
	}
}

