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
package com.corn.trade.model;

import com.corn.trade.entity.Asset;
import com.corn.trade.type.TimeFrame;

@SuppressWarnings("unused")
public class Bar {
	private final double    open;
	private final double    close;
	private final double    high;
	private final double    low;
	private final long      volume;
	private final Asset     asset;
	private final long      time;
	private final TimeFrame timeFrame;

	private Bar(double open,
	           double close,
	           double high,
	           double low,
	           long volume,
	           long time,
	           Asset asset,
	           TimeFrame timeFrame) {
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = volume;
		this.asset = asset;
		this.time = time;
		this.timeFrame = timeFrame;
	}

	public long getTime() {
		return time;
	}

	public Asset getTicker() {
		return asset;
	}

	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public double getOpen() {
		return open;
	}

	public double getClose() {
		return close;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public long getVolume() {
		return volume;
	}

	public static BarBuilder aBar() {
		return new BarBuilder();
	}


	public static final class BarBuilder {
		private double    open;
		private double    close;
		private double    high;
		private double    low;
		private long      volume;
		private Asset     asset;
		private long      time;
		private TimeFrame timeFrame;

		private BarBuilder() {
		}

		public static BarBuilder aBar() {
			return new BarBuilder();
		}

		public BarBuilder withOpen(double open) {
			this.open = open;
			return this;
		}

		public BarBuilder withClose(double close) {
			this.close = close;
			return this;
		}

		public BarBuilder withHigh(double high) {
			this.high = high;
			return this;
		}

		public BarBuilder withLow(double low) {
			this.low = low;
			return this;
		}

		public BarBuilder withVolume(long volume) {
			this.volume = volume;
			return this;
		}

		public BarBuilder withAsset(Asset asset) {
			this.asset = asset;
			return this;
		}

		public BarBuilder withTime(long time) {
			this.time = time;
			return this;
		}

		public BarBuilder withTimeFrame(TimeFrame timeFrame) {
			this.timeFrame = timeFrame;
			return this;
		}

		public Bar build() {
			return new Bar(open, close, high, low, volume, time, asset, timeFrame);
		}
	}
}
