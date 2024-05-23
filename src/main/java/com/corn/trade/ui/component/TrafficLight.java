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
import java.awt.geom.Ellipse2D;

public class TrafficLight extends JPanel {
	private static final int BORDER_WIDTH = 1;
	private static final int   DIAMETER       = 20;
	private static final int   PADDING      = 5;
	private final Color activeRed    = Color.RED;
	private final Color activeGreen    = Color.GREEN;
	private final Color inactiveRed    = Color.RED.darker().darker();
	private final Color inactiveGreen  = Color.GREEN.darker().darker();

	private String activeLight = null;

	public TrafficLight() {
		setPreferredSize(new Dimension(2 * DIAMETER + 3 * PADDING, DIAMETER + 2 * PADDING));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawLight(g2d, PADDING, "red".equals(activeLight) ? activeRed : inactiveRed);
		drawLight(g2d, DIAMETER + 2 * PADDING, "green".equals(activeLight) ? activeGreen : inactiveGreen);

		g2d.dispose();
	}

	private void drawLight(Graphics2D g2d, int x, Color color) {
		// Draw the colored light
		g2d.setColor(color);
		g2d.fill(new Ellipse2D.Double(x, PADDING, DIAMETER, DIAMETER));

		// Draw the border
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(BORDER_WIDTH));
		g2d.draw(new Ellipse2D.Double(x, PADDING, DIAMETER, DIAMETER));
	}

	private void setLight(String light) {
		activeLight = light;
		repaint();
	}

	public void setRed() {
		setLight("red");
	}

	public void setGreen() {
		setLight("green");
	}
}
