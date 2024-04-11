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
