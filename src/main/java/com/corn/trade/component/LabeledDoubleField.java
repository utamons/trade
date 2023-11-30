package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LabeledDoubleField extends JPanel {

	private final JTextField textField;
	private       Color      textFieldColor;

	// Constructor
	public LabeledDoubleField(String labelText,
	                          int columns,
	                          Color textColor,
	                          int padding,
	                          int height,
	                          Consumer<Double> consumer) {
		// Initialize label and text field
		JLabel label = new JLabel(labelText);
		textField = new JTextField(columns);
		this.setMaximumSize(new Dimension(5000, height));
		this.setMinimumSize(new Dimension(50, height));
		this.setPreferredSize(new Dimension(100, height));
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

	public boolean isValidDouble() {
		try {
			double value = Double.parseDouble(textField.getText());
			return value > 0;
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
		else if (value <= 0) {
			textField.setText(value.toString());
			setError(true);
		} else {
			setError(false);
			textField.setText(String.format("%.2f", value));
		}
	}

	public Color getTextFieldColor() {
		return textFieldColor;
	}

	public void setTextFieldColor(Color color) {
		this.textFieldColor = color;
		textField.setForeground(color);
	}

	public void setError(boolean error) {
		if (error && textField.getText().isEmpty())
			textField.setBackground(Color.RED);
		else if (error)
			textField.setForeground(Color.RED);
		else {
			textField.setForeground(UIManager.getColor("TextField.foreground"));
			textField.setBackground(UIManager.getColor("TextField.background"));
		}
	}
}
