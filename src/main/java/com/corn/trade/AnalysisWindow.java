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
package com.corn.trade;

import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.ui.component.ColorfulTextWindow;
import com.corn.trade.ui.view.ParamPanel;
import com.corn.trade.service.TradeCalc;
import com.corn.trade.model.TradeData;

import javax.swing.*;
import java.awt.*;

import static com.corn.trade.util.Util.showErrorDlg;

public class AnalysisWindow extends BaseWindow {

	private ParamPanel paramPanel;

	private int count = 0;

	public AnalysisWindow(String[] args) {
		super(args, "Trade Analysis", new Dimension(700, 700));
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			AnalysisWindow analysisWindow = new AnalysisWindow(args);
			analysisWindow.display();
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Application is closing, releasing JPA resources...");
			JpaUtil.close(); // Assuming JPAUtil.close() is a static method to close EntityManagerFactory
		}));
	}

	@Override
	protected void initializeComponents() {
		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		ColorfulTextWindow textWindow = new ColorfulTextWindow(new Dimension(650, 180));

		paramPanel = new ParamPanel(new Dimension(650, 220), new Dimension(650, 180), 5, FIELD_HEIGHT,
		                                       (tradeData) -> {
			                                       try {
													   BaseWindow.loadProperties();
													   TradeData td = tradeData;
													   if (tradeData.getLevel() != null && tradeData.getPrice() == null) {
														   td = tradeData.copy().withPrice(tradeData.getLevel()).build();
													   }
													   long start = System.currentTimeMillis();
				                                       TradeCalc tradeCalc = new TradeCalc(td);
													   long end = System.currentTimeMillis();
													   paramPanel.populate(tradeCalc.getTradeData());
				                                       TradeData out = tradeCalc.calculate();
				                                       log.info("Calculation time: {} ms", end - start);
													   if (out.getTradeError() != null) {
														   textWindow.appendText("#"+count+" - doesn't fit to risks", Color.RED.darker(), true);
														   textWindow.appendText(out.toSourceParams());
													   } else {
														   textWindow.appendText("#"+count+" - good to go", Color.GREEN.darker(), true);
														   textWindow.appendText(out.toString());
													   }
													   textWindow.appendText("------------------------------------------------------");
													   count++;
			                                       } catch (Exception e) {
				                                       showErrorDlg(frame, e.getMessage(), true);
			                                       }
		                                       });

		paramPanel.onClear(() -> {
			textWindow.clear();
			count = 0;
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
