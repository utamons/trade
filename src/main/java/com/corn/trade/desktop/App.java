package com.corn.trade.desktop;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.github.weisj.darklaf.theme.Theme;

import javax.swing.*;
import java.awt.*;

public class App {

	public static final Theme DARK_THEME = new OneDarkTheme();
	public static final Theme LIGHT_THEME = new IntelliJTheme();

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			LafManager.install();

			setDefaultFont("Arial", Font.PLAIN, 14);

			JFrame frame = new JFrame("Light/Dark Mode Toggle");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500, 500);

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
			table.setEnabled(true);

			JLabel quantityLabel = new JLabel("Quantity:");
			JTextField quantityField = new JTextField(10);
			quantityField.setText("3.141592");
			quantityField.setEnabled(false);
			quantityField.setDisabledTextColor(Color.MAGENTA);
			JPanel topPanel = new JPanel();
			topPanel.add(quantityLabel);
			topPanel.add(quantityField);


			JToggleButton toggleButton = getjToggleButton(table);
			toggleButton.setMaximumSize(new Dimension(50, 50));
			JPanel bottomPanel = new JPanel();
			bottomPanel.add(toggleButton);

			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(topPanel, BorderLayout.NORTH);
			frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

			frame.setLocationRelativeTo(null);
			frame.pack();

			frame.setVisible(true);
		});
	}

	private static JToggleButton getjToggleButton(JTable table) {
		JToggleButton toggleButton = new JToggleButton("Dark Mode");

		// Action listener for the toggle button
		toggleButton.addActionListener(e -> {
			if (toggleButton.isSelected()) {
				// Dark mode
				LafManager.install(DARK_THEME);
				table.setForeground(Color.GREEN);
				toggleButton.setText("Light Mode");
			} else {
				// Light mode
				LafManager.install(LIGHT_THEME);
				table.setForeground(Color.BLACK);
				toggleButton.setText("Dark Mode");
			}
		});
		return toggleButton;
	}

	private static void setDefaultFont(String fontName, int style, int size) {
		UIManager.put("Label.font", new Font(fontName, style, size));
		UIManager.put("Button.font", new Font(fontName, style, size));
		// Add other components here

		// For a comprehensive approach, iterate over all keys
		for (Object key : UIManager.getDefaults().keySet()) {
			if (UIManager.get(key) instanceof Font) {
				UIManager.put(key, new Font(fontName, style, size));
			}
		}
	}
}
