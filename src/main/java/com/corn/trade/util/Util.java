package com.corn.trade.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {
	public static BigDecimal round(Double value) {
		if (value == null)
			return null;
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN);
	}
}
