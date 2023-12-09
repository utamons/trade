package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class TrafficLight extends JPanel {
	private static final int   DIAMETER       = 20;
	private static final int   PADDING      = 5;
	private final Color activeRed    = Color.RED;
	private final Color activeYellow = Color.YELLOW;
	private final Color activeGreen    = Color.GREEN;
	private final Color inactiveRed    = Color.LIGHT_GRAY;
	private final Color inactiveYellow = Color.LIGHT_GRAY;
	private final Color inactiveGreen  = Color.LIGHT_GRAY;

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

		// Draw the red light
		g2d.setColor("red".equals(activeLight) ? activeRed : inactiveRed);
		g2d.fill(new Ellipse2D.Double(PADDING, PADDING, DIAMETER, DIAMETER));

		// Draw the yellow light
		g2d.setColor("yellow".equals(activeLight) ? activeYellow : inactiveYellow);
		g2d.fill(new Ellipse2D.Double(DIAMETER + 2 * PADDING, PADDING, DIAMETER, DIAMETER));

		// Draw the green light
		g2d.setColor("green".equals(activeLight) ? activeGreen : inactiveGreen);
		g2d.fill(new Ellipse2D.Double(2 * DIAMETER + 3 * PADDING, PADDING, DIAMETER, DIAMETER));

		g2d.dispose();
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
