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
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomTitleBar extends JPanel {
	public CustomTitleBar(String title, JFrame frame) {
		JLabel titleLabel = new JLabel(title);

		Border       outPadding  = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		Border       lineBorder   = BorderFactory.createLineBorder(Color.LIGHT_GRAY);

		Border compoundBorder = BorderFactory.createCompoundBorder(outPadding, lineBorder);

		Border innerPadding = BorderFactory.createEmptyBorder(0, 5, 0, 5);

		this.setBorder(BorderFactory.createCompoundBorder(compoundBorder, innerPadding));

		// Control buttons
		JButton closeButton = new JButton("X");
		closeButton.setFont(new Font("Arial", Font.PLAIN, 11));
		JButton minimizeButton = new JButton("_");
		minimizeButton.setFont(new Font("Arial", Font.PLAIN, 11));


		setLayout(new BorderLayout());

		// Customize titleLabel as needed
		add(titleLabel, BorderLayout.WEST);

		// Add functionality to buttons
		closeButton.addActionListener(e -> {
			frame.dispose();
			System.exit(0);
		});
		minimizeButton.addActionListener(e -> frame.setState(Frame.ICONIFIED));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(minimizeButton);
		buttonPanel.add(closeButton);
		// Customize buttons as needed
		add(buttonPanel, BorderLayout.EAST);

		// Mouse listener to support dragging the window
		MouseAdapter ma = new MouseAdapter() {
			private Point initialClick;

			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				initialClick = e.getPoint();
				getComponentAt(initialClick);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// get location of Window
				int thisX = frame.getLocation().x;
				int thisY = frame.getLocation().y;

				// Determine how much the mouse moved since the initial click
				int xMoved = e.getX() - initialClick.x;
				int yMoved = e.getY() - initialClick.y;

				// Move window to this position
				int X = thisX + xMoved;
				int Y = thisY + yMoved;
				frame.setLocation(X, Y);
			}
		};
		addMouseListener(ma);
		addMouseMotionListener(ma);
	}
}
