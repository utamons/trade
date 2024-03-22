package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;
import com.corn.trade.ui.component.InfoField;

public class PosInfoRow extends JPanel {
	private final InfoField qttField, slField, beField, psField, plField;
	private final JLabel label;

	public PosInfoRow(String labelText, int fontSize, int padding, int hgap, int hSpacing) {
		// Set the component layout
		setLayout(new GridLayout(1, 6, hgap, 0));
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		//setBackground(Color.LIGHT_GRAY);

		label = new JLabel(labelText);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, fontSize));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hSpacing, 0));
		labelPanel.add(label);

		qttField = new InfoField("Qtt:", fontSize, 0, hSpacing, 30);
		slField = new InfoField("Sl:", fontSize, 0, hSpacing, 30);
		beField = new InfoField("Be:", fontSize, 0, hSpacing,30);
		psField = new InfoField("Ps:", fontSize, 0, hSpacing,30);
		plField = new InfoField("P/L:", fontSize, 0, hSpacing, 30);

		add(labelPanel);
		add(qttField);
		add(slField);
		add(beField);
		add(psField);
		add(plField);
	}

	public JLabel getLabel() {
		return label;
	}

	public InfoField getQttField() {
		return qttField;
	}

	public InfoField getSlField() {
		return slField;
	}

	public InfoField getBeField() {
		return beField;
	}

	public InfoField getPsField() {
		return psField;
	}

	public InfoField getPlField() {
		return plField;
	}
}
