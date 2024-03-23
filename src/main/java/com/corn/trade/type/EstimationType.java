package com.corn.trade.type;

import java.util.List;

public enum EstimationType {
	MIN_GOAL,
	MAX_STOP_LOSS;

	public static final String MIN_GOAL_STR      = "Min. Goal";
	public static final String MAX_STOP_LOSS_STR = "Max. Stop";

	public String toString() {
		return switch (this) {
			case MIN_GOAL -> MIN_GOAL_STR;
			case MAX_STOP_LOSS -> MAX_STOP_LOSS_STR;
		};
	}

	public static EstimationType fromString(String string) {
		return switch (string) {
			case MIN_GOAL_STR -> MIN_GOAL;
			case MAX_STOP_LOSS_STR -> MAX_STOP_LOSS;
			default -> throw new IllegalArgumentException("Invalid estimation type: " + string);
		};
	}

	public static List<String> getValues() {
		return List.of(MIN_GOAL.toString(), MAX_STOP_LOSS.toString());
	}
}
