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
			return "null";
		}
		return String.format("%.2f", value);
	}

	public static Double round(Double value) {
		if (value == null) {
			return null;
		}
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
