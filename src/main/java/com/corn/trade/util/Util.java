package com.corn.trade.util;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

	public static void showErrorDlg(Component frame, String error) {
		JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarningDlg(Component frame, String warning) {
		JOptionPane.showMessageDialog(frame, warning, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public static void log(String message, Object... args) {
		int argIndex = 0;
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < message.length(); i++) {
			if (i < message.length() - 1 && message.charAt(i) == '{' && message.charAt(i + 1) == '}') {
				if (argIndex < args.length) {
					builder.append(args[argIndex++]);
				} else {
					builder.append("{}");
				}
				i++; // Skip next character
			} else {
				builder.append(message.charAt(i));
			}
		}

		System.out.println(builder);
	}
}
