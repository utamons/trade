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
import java.awt.*;

public class RowPanel extends JPanel {

	private final JPanel jPanel;

	public RowPanel(int vGap) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(vGap, 0, vGap, 0));

		jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		add(jPanel, BorderLayout.CENTER);
	}

	@Override
	public Component add(Component comp) {
		jPanel.add(comp);
		return comp;
	}
}

