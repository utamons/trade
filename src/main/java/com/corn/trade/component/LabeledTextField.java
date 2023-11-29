package com.corn.trade.component;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

@SuppressWarnings("unused")
public class LabeledTextField extends JPanel {

	private final JTextField textField;
	private Color textFieldColor;

	// Constructor
	public LabeledTextField(String labelText, int columns, Color textColor, int padding, int height) {
		// Initialize label and text field
		JLabel label = new JLabel(labelText);
		textField = new JTextField(columns);
		this.setMaximumSize(new Dimension(5000,height));
		this.setMinimumSize(new Dimension(50,height));
		this.setPreferredSize(new Dimension(100,height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);
		this.textFieldColor = textColor;

		// Set text field color
		if (textColor != null)
			textField.setForeground(textColor);

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);
		add(textField, BorderLayout.EAST);
	}

	// Method to validate the text field content as a double
	public boolean isValidDouble() {
		try {
			Double.parseDouble(textField.getText());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
	}

	// Getter for the text field value
	public String getText() {
		return textField.getText();
	}

	// Setter for the text field value
	public void setText(String text) {
		textField.setText(text);
	}

	// Getter for the text field color
	public Color getTextFieldColor() {
		return textFieldColor;
	}

	// Setter for the text field color
	public void setTextFieldColor(Color color) {
		this.textFieldColor = color;
		textField.setForeground(color);
	}
}
