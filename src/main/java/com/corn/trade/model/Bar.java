package com.corn.trade.model;

import com.corn.trade.entity.Asset;
import com.corn.trade.type.TimeFrame;

@SuppressWarnings("unused")
public class Bar {
	private final double open;
	private final double close;
	private final double high;
	private final double low;
	private final double    volume;
	private final Asset     asset;
	private final TimeFrame timeFrame;

	public Bar(double open, double close, double high, double low, double volume, Asset asset, TimeFrame timeFrame) {
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volume = volume;
		this.asset = asset;
		this.timeFrame = timeFrame;
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

	public double getVolume() {
		return volume;
	}
}
