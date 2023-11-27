package com.corn.trade.web.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

	private Util() {
		throw new IllegalStateException("Utility class");
	}

	public static BigDecimal round(Double value) {
		if (value == null)
			return null;
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN);
	}
	public static Double round(Double value, int scale) {
		if (value == null)
			return null;
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
	}
}
