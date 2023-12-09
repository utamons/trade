package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.log;

@SuppressWarnings("unused")
public class LabeledDoubleField extends JPanel {

	private final JTextField textField;
	private final JCheckBox  autoSwitch;
	private final Border     errorBorder;
	private final JLabel     lightIndicator;
	private       Color      textFieldColor;
	private       boolean    autoUpdate;

	// Constructor
	public LabeledDoubleField(String labelText,
	                          int columns,
	                          Color textColor,
	                          int padding,
	                          int height,
	                          boolean hasAutoSwitch,
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
		this.errorBorder = BorderFactory.createLineBorder(Color.RED, 3);

		if (textColor != null)
			textField.setForeground(textColor);

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!textField.getText().isEmpty() && !isValidDouble())
					setError(true);
				else {
					setError(false);
					feedConsumer(consumer);
				}
			}
		});

		textField.addActionListener(e -> {
			log("Action performed");
			if (!textField.getText().isEmpty() && !isValidDouble())
				setError(true);
			else {
				setError(false);
				feedConsumer(consumer);
			}
		});

		lightIndicator = new JLabel();

		autoSwitch = new JCheckBox();

		autoUpdate = !hasAutoSwitch;

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

		autoSwitch.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		lightIndicator.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 2));

		panel.add(lightIndicator);
		panel.add(autoSwitch);
		panel.add(textField);

		add(panel, BorderLayout.EAST);
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
		if (!autoUpdate)
			return;
		if (value == null) {
			textField.setText("");
		} else if (value <= 0) {
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

	public void setAutoSwitchVisible(boolean visible) {
		autoSwitch.setVisible(visible);
		autoUpdate = visible;
		textField.setEditable(!autoUpdate);
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		textField.setEditable(!autoUpdate);
	}

	public void light(boolean on, Color color) {
		lightIndicator.setIcon(new ColoredCircleIcon(on, color, 10));
		lightIndicator.repaint();
	}
}
