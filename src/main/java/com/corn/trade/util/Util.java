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
package com.corn.trade.util;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

	public static void showErrorDlg(Component frame, String error, boolean errorEnabled) {
		if (errorEnabled)
			JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarningDlg(Component frame, String warning) {
		JOptionPane.showMessageDialog(frame, warning, "Warning", JOptionPane.WARNING_MESSAGE);
	}


	public static String fmt(Double value) {
		if (value == null) {
			return "n/a";
		}
		return String.format("%.2f", value);
	}

	public static Double round(Double value) {
		if (value == null) {
			return null;
		}
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public static BigDecimal toBigDecimal(Double value) {
		if (value == null) {
			return null;
		}
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
	}
}
