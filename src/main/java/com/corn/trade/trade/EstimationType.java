package com.corn.trade.trade;

public enum EstimationType {
	MAX_STOP_LOSS,
	MIN_STOP_LOSS,
	MAX_GAIN_MAX_STOP_LOSS,
	MAX_GAIN_MIN_STOP_LOSS;

	public static final String MAX_STOP_LOSS_STR          = "Max. Stop Loss";
	public static final String MAX_GAIN_MAX_STOP_LOSS_STR = "Max. Gain/Max. Stop";

	public static final String MIN_STOP_LOSS_STR = "Min. Stop Loss";

	public static final String MAX_GAIN_MIN_STOP_LOSS_STR = "Max. Gain/Min. Stop";

	public String toString() {
		return switch (this) {
			case MAX_STOP_LOSS -> MAX_STOP_LOSS_STR;
			case MAX_GAIN_MAX_STOP_LOSS -> MAX_GAIN_MAX_STOP_LOSS_STR;
			case MIN_STOP_LOSS -> MIN_STOP_LOSS_STR;
			case MAX_GAIN_MIN_STOP_LOSS -> MAX_GAIN_MIN_STOP_LOSS_STR;
		};
	}

	public static EstimationType fromString(String string) {
		return switch (string) {
			case MAX_STOP_LOSS_STR -> MAX_STOP_LOSS;
			case MAX_GAIN_MAX_STOP_LOSS_STR -> MAX_GAIN_MAX_STOP_LOSS;
			case MIN_STOP_LOSS_STR -> MIN_STOP_LOSS;
			case MAX_GAIN_MIN_STOP_LOSS_STR -> MAX_GAIN_MIN_STOP_LOSS;
			default -> throw new IllegalArgumentException("Invalid estimation type: " + string);
		};
	}
}
