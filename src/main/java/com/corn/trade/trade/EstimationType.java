package com.corn.trade.trade;

public enum EstimationType {
	MAX_STOP_LOSS,
	MAX_GAIN;

	public static final String MAX_STOP_LOSS_STR = "Max. Stop Loss";
	public static final String MAX_GAIN_STR      = "Max. Gain";

	public String toString() {
		return switch (this) {
			case MAX_STOP_LOSS -> MAX_STOP_LOSS_STR;
			case MAX_GAIN -> MAX_GAIN_STR;
		};
	}

	public static EstimationType fromString(String string) {
		return switch (string) {
			case MAX_STOP_LOSS_STR -> MAX_STOP_LOSS;
			case MAX_GAIN_STR -> MAX_GAIN;
			default -> throw new IllegalArgumentException("Invalid estimation type: " + string);
		};
	}
}
