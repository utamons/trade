package com.corn.trade.desktop;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.JMarsDarkTheme;
import mdlaf.themes.MaterialLiteTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 */
public class App {
	private static final Point point = new Point();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialLiteTheme()));
			} catch (UnsupportedLookAndFeelException e) {
				throw new RuntimeException(e);
			}

			JFrame frame = new JFrame("Light/Dark Mode Toggle");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500, 500);

			// Custom title bar panel
			JPanel titleBar = new JPanel();
			//titleBar.setBackground(Color.GRAY); // Example color
			titleBar.setPreferredSize(new Dimension(500, 30)); // Set preferred size

			// Title label
			JLabel titleLabel = new JLabel("My Custom Title Bar", JLabel.CENTER);
			titleBar.add(titleLabel);

			// Close button
			JButton closeButton = new JButton("X");
			closeButton.addActionListener(e -> System.exit(0));
			titleBar.add(closeButton);

			// Add mouse listener for dragging the window
			titleBar.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					point.x = e.getX();
					point.y = e.getY();
				}
			});
			titleBar.addMouseMotionListener(new MouseAdapter() {
				public void mouseDragged(MouseEvent e) {
					Point p = frame.getLocation();
					frame.setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
					System.out.println(frame.getLocation().x + " " + frame.getLocation().y);
				}
			});

			frame.getContentPane().add(titleBar, BorderLayout.NORTH);

			// Table data and column names
			String[] columnNames = {"Column 1", "Column 2"};
			Object[][] data = {
					{"Row 1, Col 1", "Row 1, Col 2"},
					{"Row 2, Col 1", "Row 2, Col 2"}
			};

			// Creating the table
			JTable table = new JTable(data, columnNames);
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(false);
			table.setPreferredScrollableViewportSize(new Dimension(500, 70));
			table.setEnabled(false);

			JLabel quantityLabel = new JLabel("Quantity:");
			JTextField quantityField = new JTextField(10);
			quantityField.setText("3.141592");
			quantityField.setEnabled(false);
			quantityField.setDisabledTextColor(Color.MAGENTA);
			JPanel topPanel = new JPanel();
			topPanel.add(quantityLabel);
			topPanel.add(quantityField);


			JToggleButton toggleButton = getjToggleButton(frame);

			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(titleBar, BorderLayout.NORTH);
			//  frame.getContentPane().add(topPanel, BorderLayout.NORTH);
			frame.getContentPane().add(toggleButton, BorderLayout.SOUTH);
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER); // Add the table in a scroll pane

			frame.setUndecorated(true);
			frame.setLocationRelativeTo(null);
			frame.pack();

			frame.setVisible(true);
		});
	}

	private static JToggleButton getjToggleButton(JFrame frame) {
		JToggleButton toggleButton = new JToggleButton("Toggle Light/Dark Mode");

		// Action listener for the toggle button
		toggleButton.addActionListener(e -> {
			if (toggleButton.isSelected()) {
				// Dark mode
				try {
					UIManager.setLookAndFeel(new MaterialLookAndFeel(new JMarsDarkTheme()));
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
				} catch (UnsupportedLookAndFeelException eX) {
					throw new RuntimeException(eX);
				}
			} else {
				// Light mode
				try {
					UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialLiteTheme()));
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
				} catch (UnsupportedLookAndFeelException eX) {
					throw new RuntimeException(eX);
				}
			}
		});
		return toggleButton;
	}
}
