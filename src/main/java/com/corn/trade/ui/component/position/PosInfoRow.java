package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;
import com.corn.trade.ui.component.InfoField;

import static com.corn.trade.util.Util.fmt;

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

	public void setQtt(long qtt) {
		qttField.setInfoText(String.valueOf(qtt));
	}

	public void setSl(Double sl) {
		slField.setInfoText(fmt(sl));
	}

	public void setBe(Double be) {
		beField.setInfoText(fmt(be));
	}

	public void setPs(Double ps) {
		psField.setInfoText(fmt(ps), true);
	}

	public void setPl(Double pl) {
		plField.setInfoText(fmt(pl));
	}

	public void setPsColor(Color color) {
		psField.setInfoFieldColor(color);
	}

	public void setPlColor(Color color) {
		plField.setInfoFieldColor(color);
	}
}
