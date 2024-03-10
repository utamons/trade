package com.corn.trade.panel.calculator;

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
}
