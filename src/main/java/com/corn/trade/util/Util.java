package com.corn.trade.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {
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

	public static double round(double value) {
		return BigDecimal.valueOf(value)
		                 .setScale(2, RoundingMode.HALF_UP)
		                 .doubleValue();
	}
}
