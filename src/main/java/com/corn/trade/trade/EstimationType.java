package com.corn.trade.trade;

public enum EstimationType {
	MAX_STOP_LOSS,
	MAX_TAKE_PROFIT;

	public static final String MAX_STOP_LOSS_STR = "Max. Stop Loss";
	public static final String MAX_TAKE_PROFIT_STR = "Max. Take Profit";

	public String toString() {
		return switch (this) {
			case MAX_STOP_LOSS -> MAX_STOP_LOSS_STR;
			case MAX_TAKE_PROFIT -> MAX_TAKE_PROFIT_STR;
		};
	}

	public static EstimationType fromString(String string) {
		return switch (string) {
			case MAX_STOP_LOSS_STR -> MAX_STOP_LOSS;
			case MAX_TAKE_PROFIT_STR -> MAX_TAKE_PROFIT;
			default -> throw new IllegalArgumentException("Invalid estimation type: " + string);
		};
	}
}
