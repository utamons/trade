package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class TrafficLight extends JPanel {
	private static final int BORDER_WIDTH = 1;
	private static final int   DIAMETER       = 20;
	private static final int   PADDING      = 5;
	private final Color activeRed    = Color.RED;
	private final Color activeYellow = Color.YELLOW;
	private final Color activeGreen    = Color.GREEN;
	private final Color inactiveRed    = Color.RED.darker().darker();
	private final Color inactiveYellow = Color.YELLOW.darker().darker();
	private final Color inactiveGreen  = Color.GREEN.darker().darker();

	private String activeLight = null;

	public TrafficLight() {
		setPreferredSize(new Dimension(3 * DIAMETER + 4 * PADDING, DIAMETER + 2 * PADDING));
		//setBackground(Color.WHITE);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawLight(g2d, PADDING, "red".equals(activeLight) ? activeRed : inactiveRed);
		drawLight(g2d, DIAMETER + 2 * PADDING, "yellow".equals(activeLight) ? activeYellow : inactiveYellow);
		drawLight(g2d, 2 * DIAMETER + 3 * PADDING, "green".equals(activeLight) ? activeGreen : inactiveGreen);

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

	public void setYellow() {
		setLight("yellow");
	}
}
