package com.corn.trade.component;
import com.corn.trade.util.DoubleConsumer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

@SuppressWarnings("unused")
public class LabeledDoubleField extends JPanel {

	private final JTextField textField;
	private Color textFieldColor;

	// Constructor
	public LabeledDoubleField(String labelText, int columns, Color textColor, int padding, int height, DoubleConsumer consumer) {
		// Initialize label and text field
		JLabel label = new JLabel(labelText);
		textField = new JTextField(columns);
		this.setMaximumSize(new Dimension(5000,height));
		this.setMinimumSize(new Dimension(50,height));
		this.setPreferredSize(new Dimension(100,height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);
		this.textFieldColor = textColor;

		if (textColor != null)
			textField.setForeground(textColor);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!textField.getText().isEmpty()) {
					if (!isValidDouble())
						textField.setForeground(Color.RED);
					else if (consumer != null) {
						consumer.accept(Double.parseDouble(textField.getText()));
						textField.setForeground(textFieldColor);
					}
				}
			}
		});

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

	public Double getValue() {
		return Double.parseDouble(textField.getText());
	}

	public void setValue(Double value) {
		if (value == null)
			textField.setText("");
		else
			textField.setText(value.toString());
	}

	public Color getTextFieldColor() {
		return textFieldColor;
	}

	public void setTextFieldColor(Color color) {
		this.textFieldColor = color;
		textField.setForeground(color);
	}
}
