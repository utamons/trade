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
package com.corn.trade.ui.view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public abstract class BasePanel extends JPanel {
	public BasePanel(Dimension preferredSize, Dimension minSize) {
		super();
		LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		Border       emptyBorder  = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		this.setBorder(emptyBorder);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(minSize);
		this.setLayout(layout);
	}
}
