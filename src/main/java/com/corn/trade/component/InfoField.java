package com.corn.trade.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class InfoField extends JPanel {
	private final JLabel infoField;
	private Timer blinkTimer;
	private boolean isBlinking = false;
	//private Color originalColor = UIManager.getColor("Label.foreground");

	//private final Color originalBackground = UIManager.getColor("Panel.background");

	public InfoField(String labelText, int padding, int horizontalSpacing, int height) {
		JLabel label = new JLabel(labelText);
		infoField = new JLabel();

		setMaximumSize(new Dimension(100, height));
		setMinimumSize(new Dimension(50, height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		setBorder(emptyBorder);

		JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, horizontalSpacing, 0));
		contentPanel.add(label);
		contentPanel.add(infoField);

		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.WEST);

		infoField.setForeground(getOriginalColor());
	}

	private Color getOriginalColor() {
		return UIManager.getColor("Label.foreground");
	}

	private Color getOriginalBackground() {
		return UIManager.getColor("Panel.background");
	}

	public void setInfoFieldColor(Color color) {
		if (!isBlinking) {
			infoField.setForeground(color);
		}
	}

	public void setBold(boolean bold) {
		Font font = infoField.getFont();
		if (bold) {
			infoField.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		} else {
			infoField.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
		}
	}

	public void startBlinking(Color blinkColor, int blinkRate) {
		if (blinkTimer != null) {
			blinkTimer.stop();
		}
		blinkTimer = new Timer(blinkRate, e -> {
			if (infoField.getForeground().equals(getOriginalBackground())) {
				infoField.setForeground(blinkColor);
			} else {
				infoField.setForeground(getOriginalBackground());
			}
		});
		isBlinking = true;
		infoField.setForeground(getOriginalBackground());
		blinkTimer.start();
	}

	public void stopBlinking() {
		if (blinkTimer != null) {
			blinkTimer.stop();
			infoField.setForeground(getOriginalColor());
			isBlinking = false;
		}
	}

	public void setInfoText(String text) {
		infoField.setText(text);
	}
}
