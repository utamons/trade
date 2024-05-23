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
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;

public class ColorfulTextWindow extends JScrollPane {
	public static final float          LINE_SPACING = 0.2f;
	private final JTextPane      textPane;
	private final StyledDocument doc;

	public ColorfulTextWindow(Dimension size) {

		Border       emptyBorder  = BorderFactory.createEmptyBorder(0, 5, 5, 5);
		Border       lineBorder   = BorderFactory.createLineBorder(Color.LIGHT_GRAY); // Padding
		TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "Output");
		titledBorder.setTitleFont(new Font("Arial", Font.PLAIN, 12));
		titledBorder.setTitleColor(Color.GRAY);
		Border compoundBorder = BorderFactory.createCompoundBorder(emptyBorder, titledBorder);

		this.setBorder(BorderFactory.createCompoundBorder(compoundBorder, emptyBorder));

		setPreferredSize(size);
		setMinimumSize(size);
		textPane = new JTextPane();
		textPane.setEditable(false);
		setViewportView(textPane);

		doc = textPane.getStyledDocument();
		textPane.selectAll();
		MutableAttributeSet set = new SimpleAttributeSet(textPane.getParagraphAttributes());
		StyleConstants.setLineSpacing(set, LINE_SPACING);
		textPane.setParagraphAttributes(set, true);
	}

	public void appendText(String text, Color color, boolean bold) {
		Style style = textPane.addStyle("ColoredText", null);
		StyleConstants.setForeground(style, color);
		StyleConstants.setBold(style, bold);

		try {
			doc.insertString(doc.getLength(), text + "\n", style);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	public void appendText(String text) {
		try {
			doc.insertString(doc.getLength(), text + "\n", null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	public void clear() {
		textPane.setText("");
	}
}
