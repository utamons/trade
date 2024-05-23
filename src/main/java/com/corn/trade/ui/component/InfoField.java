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

public class InfoField extends JPanel {
	private final JLabel infoField;
	private Timer blinkTimer;
	private boolean isBlinking = false;

	public InfoField(String labelText, int fontSize, int padding, int horizontalSpacing, int height) {
		JLabel label = new JLabel(labelText);
		infoField = new JLabel();

		setMaximumSize(new Dimension(100, height));
		Border emptyBorder = BorderFactory.createEmptyBorder(padding, padding, padding, padding);
		if (padding > 0)
			setBorder(emptyBorder);

		JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, horizontalSpacing, 0));
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontSize));
		contentPanel.add(label);
		contentPanel.add(infoField);

		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.WEST);

		infoField.setForeground(getOriginalColor());
		infoField.setFont(new Font(infoField.getFont().getName(), Font.PLAIN, fontSize));
		infoField.setText("n/a");
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

	@SuppressWarnings("unused")
	public void setBold(boolean bold) {
		Font font = infoField.getFont();
		if (bold) {
			infoField.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		} else {
			infoField.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
		}
	}

	@SuppressWarnings("unused")
	public void startBlinking(Color blinkColor, int blinkRate) {
		if (blinkTimer != null && blinkTimer.isRunning()) {
			return;
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

	@SuppressWarnings("unused")
	public void stopBlinking() {
		if (blinkTimer != null) {
			blinkTimer.stop();
			infoField.setForeground(getOriginalColor());
			isBlinking = false;
		}
	}

	public void setInfoText(String text) {
		if (text == null || text.isEmpty()) {
			text = "n/a";
		}
		infoField.setText(text);
	}

	public void setInfoText(String text, boolean percents) {
		if (text == null || text.isEmpty()) {
			infoField.setText("n/a");
			return;
		}
		if (percents) {
			infoField.setText(text + "%");
		} else {
			infoField.setText(text);
		}
	}

	public void clear() {
		infoField.setText("n/a");
	}
}
