package com.corn.trade.component;

import com.corn.trade.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LabeledDoubleField extends JPanel {

	private final JTextField textField;
	private final JCheckBox autoSwitch;
	private       Color      textFieldColor;

	private final Consumer<Double> consumer;

	private boolean autoUpdate = false;

	// Constructor
	public LabeledDoubleField(String labelText,
	                          int columns,
	                          Color textColor,
	                          int padding,
	                          int height,
	                          boolean hasAutoSwitch,
	                          Consumer<Double> consumer) {
		// Initialize label and text field
		this.consumer = consumer;
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

		textField.addActionListener(e -> {
			if (!textField.getText().isEmpty()) {
				Util.log("Action performed");
				if (!isValidDouble())
					textField.setForeground(Color.RED);
				else if (consumer != null) {
					consumer.accept(Double.parseDouble(textField.getText()));
					textField.setForeground(textFieldColor);
				}
			}
		});

		autoSwitch = new JCheckBox();
		autoSwitch.setVisible(hasAutoSwitch);
		autoSwitch.addActionListener(e -> {
			autoUpdate = !autoSwitch.isSelected();
			textField.setEditable(!autoUpdate);
		});

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(autoSwitch);
		panel.add(textField);
		add(panel, BorderLayout.EAST);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
		if (!autoUpdate)
			return;
		if (value == null)
			textField.setText("");
		else if (value <= 0) {
			textField.setText(value.toString());
			setError(true);
		} else {
			setError(false);
			textField.setText(String.format("%.2f", value));
			if (consumer != null) {
				consumer.accept(value);
			}
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
		else if (textField.isEditable()){
			textField.setForeground(UIManager.getColor("TextField.foreground"));
			textField.setBackground(UIManager.getColor("TextField.background"));
		} else
			textField.setForeground(UIManager.getColor("TextField.foreground"));
	}

	public void setEditable(boolean editable) {
		textField.setEditable(editable);
	}

	public void setAutoSwitchVisible(boolean visible) {
		autoSwitch.setVisible(visible);
		autoUpdate = visible;
		textField.setEditable(!autoUpdate);
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		textField.setEditable(!autoUpdate);
	}
}
