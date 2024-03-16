package com.corn.trade.trade.analysis.data;

import com.corn.trade.trade.type.TimeFrame;
import com.corn.trade.trade.type.TradeZone;

import java.util.List;

public class TradeContext {
	private final double price;
	private final double spread;
	private final double high;
	private final double low;
	private final double atr;
	private final TradeZone zone;
	private final TimeFrame timeFrame;
	private final List<Bar> bars;
	private final List<Double> fiboLevels;

	private TradeContext(double price, double spread, double high, double low, double atr, TradeZone zone, TimeFrame timeFrame, List<Bar> bars, List<Double> fiboLevels) {
		this.price = price;
		this.spread = spread;
		this.high = high;
		this.low = low;
		this.atr = atr;
		this.zone = zone;
		this.timeFrame = timeFrame;
		this.bars = bars;
		this.fiboLevels = fiboLevels;
	}

	public double getPrice() {
		return price;
	}

	public double getSpread() {
		return spread;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getAtr() {
		return atr;
	}

	public TradeZone getZone() {
		return zone;
	}

	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public List<Bar> getBars() {
		return bars;
	}

	public List<Double> getFiboLevels() {
		return fiboLevels;
	}


	public static final class TradeContextBuilder {
		private double       price;
		private double       spread;
		private double       high;
		private double       low;
		private double       atr;
		private TradeZone    zone;
		private TimeFrame    timeFrame;
		private List<Bar>    bars;
		private List<Double> fiboLevels;

		private TradeContextBuilder() {
		}

		public static TradeContextBuilder aTradeContext() {
			return new TradeContextBuilder();
		}

		public TradeContextBuilder withPrice(double price) {
			this.price = price;
			return this;
		}

		public TradeContextBuilder withSpread(double spread) {
			this.spread = spread;
			return this;
		}

		public TradeContextBuilder withHigh(double high) {
			this.high = high;
			return this;
		}

		public TradeContextBuilder withLow(double low) {
			this.low = low;
			return this;
		}

		public TradeContextBuilder withAtr(double atr) {
			this.atr = atr;
			return this;
		}

		public TradeContextBuilder withZone(TradeZone zone) {
			this.zone = zone;
			return this;
		}

		public TradeContextBuilder withTimeFrame(TimeFrame timeFrame) {
			this.timeFrame = timeFrame;
			return this;
		}

		public TradeContextBuilder withBars(List<Bar> bars) {
			this.bars = bars;
			return this;
		}

		public TradeContextBuilder withFiboLevels(List<Double> fiboLevels) {
			this.fiboLevels = fiboLevels;
			return this;
		}

		public TradeContext build() {
			return new TradeContext(price, spread, high, low, atr, zone, timeFrame, bars, fiboLevels);
		}
	}
}
