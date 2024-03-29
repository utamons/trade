package com.corn.trade.trade;

public enum PositionType {
	LONG,
	SHORT;

	public static final String LONG_STR = "Long";
	public static final String SHORT_STR = "Short";

	public String toString() {
		return switch (this) {
			case LONG -> LONG_STR;
			case SHORT -> SHORT_STR;
		};
	}

	public static PositionType fromString(String string) {
		return switch (string) {
			case LONG_STR -> LONG;
			case SHORT_STR -> SHORT;
			default -> throw new IllegalArgumentException("Invalid position type: " + string);
		};
	}
}
