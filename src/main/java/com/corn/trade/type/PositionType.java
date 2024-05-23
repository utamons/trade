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

	public static List<String> getValues() {
		return List.of(LONG.toString(), SHORT.toString());
	}
}
