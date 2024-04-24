package com.corn.trade.ui.component.position;

import javax.swing.*;
import java.awt.*;
import com.corn.trade.ui.component.InfoField;

import static com.corn.trade.util.Util.fmt;

public class PosInfoRow extends JPanel {
	private final InfoField qttField, slField, beField, goalField, dstField, plField;
	private final JLabel label;

	public PosInfoRow(String labelText, int fontSize, int padding, int hgap, int hSpacing) {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		setPreferredSize(new Dimension(150, 30));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 0, 0, hgap);

		label = new JLabel(labelText);
		label.setFont(new Font(label.getFont().getName(), Font.BOLD, fontSize));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hSpacing, 0));
		labelPanel.add(label);


		beField = new InfoField("BE", fontSize, 0, hSpacing, 30);
		qttField = new InfoField("Qtt:", fontSize, 0, hSpacing, 30);
		slField = new InfoField("Sl:", fontSize, 0, hSpacing, 30);
		goalField = new InfoField("Gl:", fontSize, 0, hSpacing, 30);
		dstField = new InfoField("Dst:", fontSize, 0, hSpacing, 30);
		plField = new InfoField("P/L:", fontSize, 0, hSpacing, 30);

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		add(labelPanel, gbc);

		gbc.gridx = 1;
		add(beField, gbc);
		gbc.gridx = 2;
		add(qttField, gbc);
		gbc.gridx = 3;
		add(slField, gbc);
		gbc.gridx = 4;
		add(goalField, gbc);
		gbc.gridx = 5;
		add(dstField, gbc);
		gbc.gridx = 6;
		add(plField, gbc);
	}

	public JLabel getLabel() {
		return label;
	}

	public void setBe(Double be) {
		beField.setInfoText(fmt(be));
	}

	public void setQtt(String qtt) {
		qttField.setInfoText(qtt);
	}

	public void setSl(Double sl) {
		slField.setInfoText(fmt(sl));
	}

	public void setGoal(Double goal) {
		goalField.setInfoText(fmt(goal));
	}

	public void setDst(Double dst) {
		dstField.setInfoText(fmt(dst), true);
	}

	public void setPl(Double pl) {
		plField.setInfoText(fmt(pl));
	}

	public void setPsColor(Color color) {
		dstField.setInfoFieldColor(color);
	}

	public void setPlColor(Color color) {
		plField.setInfoFieldColor(color);
	}
}
