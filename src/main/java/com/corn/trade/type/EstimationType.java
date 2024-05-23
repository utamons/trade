/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
