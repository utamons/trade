package com.corn.trade;

import com.corn.trade.panel.calculator.ColorfulTextWindow;
import com.corn.trade.panel.calculator.ParamPanel;
import com.corn.trade.trade.Calculator;
import com.corn.trade.trade.Levels;

import javax.swing.*;
import java.awt.*;

public class Analysis extends BaseWindow {
	public Analysis() {
		super(new String[0], "Trade Analysis", new Dimension(700, 700));
		initializeComponents();
	}
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			Analysis analysisWindow = new Analysis();
			analysisWindow.display();
		});
	}

	@Override
	protected void initializeComponents() {
		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		Levels     levels     = new Levels(frame);
		Calculator calculator = new Calculator(frame, levels);

		ParamPanel paramPanel = new ParamPanel(calculator, levels, new Dimension(650, 180), new Dimension(650, 180), 5, FIELD_HEIGHT);

		ColorfulTextWindow textWindow = new ColorfulTextWindow(new Dimension(650, 180));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0; // No extra vertical space allocation
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainContainer.add(paramPanel, gbc);

		gbc.gridy = 1;
		gbc.weighty = 1; // Allocate extra space vertically to this component
		gbc.fill = GridBagConstraints.BOTH; // Allow stretching both horizontally and vertically
		mainContainer.add(textWindow, gbc);

		textWindow.appendText("Trade Analysis v. " + version + " (" + stage + ")");
		textWindow.appendText("Enter the parameters and press 'Calculate' to calculate the trade");
		textWindow.appendText("A green line", Color.GREEN.darker(), true);
		textWindow.appendText("Indicates a good trade, a red line", Color.RED.darker(), true);
		textWindow.appendText("Indicates a bad trade");

		frame.getContentPane().add(mainContainer, BorderLayout.CENTER);
	}
}
