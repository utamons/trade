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
package com.corn.trade.broker.simulation;

public class TradeContextGenerator {
	private final double MAX_PRICE = 32.5;
	private final double MIN_PRICE = 30.0;
	private final double MAX_SPREAD = 0.03;

	private double ask;
	private double bid;
	private double price;
	private double high;
	private double low;

	private double step = 0.01;

	public TradeContextGenerator(boolean fromHigh) {
		if (fromHigh) {
			ask = MAX_PRICE;
			bid = MAX_PRICE - MAX_SPREAD;
			price = MAX_PRICE;
			high = MAX_PRICE;
			low = MAX_PRICE - MAX_SPREAD;
			step = -step;
		} else {
			ask = MIN_PRICE + MAX_SPREAD;
			bid = MIN_PRICE;
			price = MIN_PRICE;
			high = MIN_PRICE;
			low = MIN_PRICE;
		}
	}

	public Context next() {
		ask += step;
		if (ask > MAX_PRICE) {
			ask = MAX_PRICE;
			step = -step;
		}
		if (ask < MIN_PRICE) {
			ask = MIN_PRICE;
			step = -step;
		}
		double spread = Math.random() * MAX_SPREAD;
		bid = ask - spread;
		price = (ask + bid) / 2;
		high = Math.max(high, price);
		low = Math.min(low, price);
		return new Context(ask, bid, price, high, low);
	}

	public record Context (double ask, double bid, double price, double high, double low) {}
}
