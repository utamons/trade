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

public enum TimeFrame {
	M1(1),
	M5(5),
	M15(15),
	M30(30),
	H1(60),
	H4(240),
	D1(1440),
	W1(10080),
	MN1(43200);

	private final int minutes;

	TimeFrame(int minutes) {
		this.minutes = minutes;
	}

	public int getMinutes() {
		return minutes;
	}
}
