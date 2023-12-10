package com.corn.trade.util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Util {

	public static void showErrorDlg(Component frame, String error, boolean errorEnabled) {
		if (errorEnabled)
			JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarningDlg(Component frame, String warning) {
		JOptionPane.showMessageDialog(frame, warning, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	public static void log(String message, Object... args) {
		int argIndex = 0;
		StringBuilder builder = new StringBuilder();
		LocalTime now = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH.mm.ss.SSS");
		String formattedTime = now.format(formatter);

		builder.append(formattedTime).append(" - ");

		for (int i = 0; i < message.length(); i++) {
			if (i < message.length() - 1 && message.charAt(i) == '{' && message.charAt(i + 1) == '}') {
				if (argIndex < args.length) {
					Object arg = args[argIndex++];
					if (arg instanceof Double doubleArg) {
						String formattedDouble = String.format("%.2f", doubleArg);
						builder.append(formattedDouble);
					} else {
						builder.append(arg);
					}
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
