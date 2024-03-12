package com.corn.trade;

import com.corn.trade.panel.analysis.ColorfulTextWindow;
import com.corn.trade.panel.analysis.ParamPanel;
import com.corn.trade.trade.analysis.TradeCalc;

import javax.swing.*;
import java.awt.*;

public class AnalysisWindow extends BaseWindow {

	public AnalysisWindow(String args[]) {
		super(args, "Trade Analysis", new Dimension(700, 700));
		initializeComponents();
	}
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			AnalysisWindow analysisWindow = new AnalysisWindow(args);
			analysisWindow.display();
		});
	}

	@Override
	protected void initializeComponents() {
		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		TradeCalc tradeCalc = new TradeCalc();

		ColorfulTextWindow textWindow = new ColorfulTextWindow(new Dimension(650, 180));

		ParamPanel paramPanel = new ParamPanel(new Dimension(650, 220), new Dimension(650, 180), 5, FIELD_HEIGHT,
		                                       (tradeData) -> {
			                                       try {
				                                       tradeCalc.calculate(tradeData);
			                                       } catch (Exception e) {
				                                      textWindow.appendText("Error: " + e.getMessage());
			                                       }
		                                       });
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

		frame.getContentPane().add(mainContainer, BorderLayout.CENTER);
	}
}
