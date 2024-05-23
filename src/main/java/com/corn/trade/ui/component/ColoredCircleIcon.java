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
package com.corn.trade.ui.component;

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

