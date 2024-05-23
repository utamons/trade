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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LabeledDoubleField extends JPanel {
	private static final Logger     log = LoggerFactory.getLogger(LabeledDoubleField.class);
	private Timer blinkTimer;
	private boolean isLightOn;

	private Color currentLightColor;
	private final        JTextField textField;
	private final JCheckBox  controlCheckBox;
	private final Border     errorBorder;
	private final JLabel     lightIndicator;
	private       Color      textFieldColor;
	private       boolean    autoUpdate;

	public LabeledDoubleField(String labelText,
	                          int columns,
	                          int padding,
	                          int height,
	                          boolean hasControlCheckBox) {
		this(labelText, columns, null, padding, height, hasControlCheckBox, null);
	}

	public LabeledDoubleField(String labelText,
	                          int columns,
	                          int padding,
	                          int height,
	                          boolean hasControlCheckBox,
	                          Consumer<Double> consumer) {
		this(labelText, columns, null, padding, height, hasControlCheckBox, consumer);
	}

	// Constructor
	public LabeledDoubleField(String labelText,
	                          int columns,
	                          Color textColor,
	                          int padding,
	                          int height,
	                          boolean hasControlCheckBox,
	                          Consumer<Double> consumer) {
		// Initialize label and text field
		JLabel label = new JLabel(labelText);
		initializeBlinkTimer();
		textField = new JTextField(columns);
		this.setMaximumSize(new Dimension(5000, height));
		this.setMinimumSize(new Dimension(50, height));
		this.setPreferredSize(new Dimension(100, height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		this.setBorder(emptyBorder);
		this.textFieldColor = textColor;
		this.errorBorder = BorderFactory.createLineBorder(Color.RED, 3);

		if (textColor != null)
			textField.setForeground(textColor);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				replaceCommasWithDots();
				if (!textField.getText().isEmpty() && !isValidDouble())
					setError(true);
				else {
					setError(false);
					feedConsumer(consumer);
				}
			}
		});

		textField.addActionListener(e -> {
			replaceCommasWithDots();
			if (!textField.getText().isEmpty() && !isValidDouble())
				setError(true);
			else {
				setError(false);
				feedConsumer(consumer);
			}
		});

		lightIndicator = new JLabel();

		controlCheckBox = new JCheckBox();

		autoUpdate = !hasControlCheckBox;

		controlCheckBox.setVisible(hasControlCheckBox);

		controlCheckBox.addActionListener(e -> {
			boolean isSelected = controlCheckBox.isSelected();
			textField.setEnabled(isSelected);
			if (!isSelected) {
				textField.setText("");
				feedConsumer(consumer);
			}
		});

		// Set layout
		setLayout(new BorderLayout());
		add(label, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		controlCheckBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		lightIndicator.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 2));

		panel.add(lightIndicator);
		panel.add(controlCheckBox);
		panel.add(textField);

		add(panel, BorderLayout.EAST);

		if (hasControlCheckBox) {
			textField.setEnabled(false);
			textField.setText("");
		}
	}

	private void initializeBlinkTimer() {
		// Blink every 500 milliseconds
		blinkTimer = new Timer(500, e -> toggleLightIndicator());
		blinkTimer.setRepeats(true);
		blinkTimer.setCoalesce(true);
	}

	private void toggleLightIndicator() {
		if (isLightOn) {
			lightIndicator.setIcon(null); // Turn off the light by setting no icon
		} else {
			// Assume you have a method getIconForColor to get the icon based on the current color
			lightIndicator.setIcon(getIconForColor(currentLightColor)); // Turn on the light
		}
		isLightOn = !isLightOn; // Toggle the state
		lightIndicator.repaint();
	}

	public void replaceCommasWithDots() {
		String text = textField.getText();
		if (text.isEmpty())
			return;
		String modifiedText = text.replace(',', '.');
		textField.setText(modifiedText);
	}

	private void feedConsumer(Consumer<Double> consumer) {
		if (textField.getText().isEmpty() && consumer != null) {
			consumer.accept(null);
			return;
		}
		if (consumer != null) {
			consumer.accept(Double.parseDouble(textField.getText()));
			textField.setForeground(textFieldColor);
		}
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

	@Override
	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
	}

	public Double getValue() {
		if (textField.getText().isEmpty())
			return null;
		return Double.parseDouble(textField.getText());
	}

	public void setValue(Double value) {
		if (value == null) {
			textField.setText("");
		} else if (value <= 0) {
			textField.setText("0");
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
		if (!textField.isEditable())
			return;
		if (error) {
			textField.setBorder(errorBorder);
		} else {
			restoreDefaultBorder();
		}
	}

	private void restoreDefaultBorder() {
		textField.setBorder((Border) UIManager.get("TextField.border"));
	}

	public void setEditable(boolean editable) {
		textField.setEditable(editable);
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		textField.setEditable(!autoUpdate);
	}

	private ColoredCircleIcon getIconForColor(Color color) {
		return new ColoredCircleIcon(true, color, 10);
	}

	public void light(boolean on, Color color, boolean blink) {
		if (on) {
			currentLightColor = color; // Assuming you have a field currentLightColor to store the current color
			lightIndicator.setIcon(getIconForColor(color)); // Show the light with the specified color
			if (blink) {
				if (!blinkTimer.isRunning()) {
					blinkTimer.start(); // Start blinking
				}
			} else {
				if (blinkTimer.isRunning()) {
					blinkTimer.stop(); // Stop blinking
				}
			}
		} else {
			if (blinkTimer.isRunning()) {
				blinkTimer.stop(); // Ensure the timer is stopped if the light is turned off
			}
			lightIndicator.setIcon(null); // Hide the light
		}
		isLightOn = on; // Update the current state
		lightIndicator.repaint();
	}

	public void lightOff() {
		light(false, currentLightColor, false);
	}

	public boolean isBlinking() {
		return blinkTimer.isRunning();
	}

	public void setControlCheckBoxState(boolean state) {
		controlCheckBox.setSelected(state);
		textField.setEnabled(state);
		if (!state) {
			textField.setText("");
		}
	}


}
