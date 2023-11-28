package com.corn.trade.desktop.panel;

import ch.qos.logback.core.Layout;
import com.corn.trade.desktop.component.ButtonRowPanel;
import com.corn.trade.desktop.component.LabeledComboBox;
import com.corn.trade.desktop.component.LabeledTextField;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class InputPanel extends JPanel {

	public InputPanel() {
		super();
		LayoutManager layout = new GridLayout(6, 1, 1, 1);
		this.setLayout(layout);


		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY); // Padding
		TitledBorder titledBorder  = BorderFactory.createTitledBorder(lineBorder, "Input section" );
		titledBorder.setTitleFont(new Font("Arial", Font.PLAIN, 12));
		titledBorder.setTitleColor(Color.GRAY);
		Border compoundBorder = BorderFactory.createCompoundBorder(emptyBorder, titledBorder);

		this.setBorder(BorderFactory.createCompoundBorder(compoundBorder, emptyBorder));

		LabeledComboBox positionBox = new LabeledComboBox("Position:", new String[]{"Long", "Short"});
		this.add(positionBox);

		LabeledComboBox estimationBox = new LabeledComboBox("Estimation:",
		                                                    new String[]{
																	"Max. stop loss.",
				                                                    "Max. take profit.",
				                                                    "Min. break even."});
		this.add(estimationBox);

		this.add(new LabeledTextField("Spread:", 10, null));
		this.add(new LabeledTextField("Power reserve:", 10, null));
		this.add(new LabeledTextField("Level:", 10, null));

		ButtonRowPanel buttonRowPanel = new ButtonRowPanel();

		buttonRowPanel.add(new JButton("Estimate"));
		buttonRowPanel.add(new JButton("Reset"));

		this.add(buttonRowPanel);
	}
}
