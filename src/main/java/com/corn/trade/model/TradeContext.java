package com.corn.trade.model;

public class TradeContext {
	private final Double ask;
	private final Double bid;
	private final Double price;
	private final Double dayHigh;
	private final Double dayLow;
	private final Double adr;

	private TradeContext(Double ask, Double bid, Double price, Double dayHigh, Double dayLow, Double adr) {
		this.ask = ask;
		this.bid = bid;
		this.price = price;
		this.dayHigh = dayHigh;
		this.dayLow = dayLow;
		this.adr = adr;
	}

	public Double getAsk() {
		return ask;
	}

	public Double getBid() {
		return bid;
	}

	public Double getPrice() {
		return price;
	}

	public Double getDayHigh() {
		return dayHigh;
	}

	public Double getDayLow() {
		return dayLow;
	}

	public Double getAdr() {
		return adr;
	}

	public static final class TradeContextBuilder {
		private Double ask;
		private Double bid;
		private Double price;
		private Double dayHigh;
		private Double dayLow;
		private Double adr;

		private TradeContextBuilder() {
		}

		public static TradeContextBuilder aTradeContext() {
			return new TradeContextBuilder();
		}

		public TradeContextBuilder withAsk(Double ask) {
			this.ask = ask;
			return this;
		}

		public TradeContextBuilder withBid(Double bid) {
			this.bid = bid;
			return this;
		}

		public TradeContextBuilder withPrice(Double price) {
			this.price = price;
			return this;
		}

		public TradeContextBuilder withDayHigh(Double dayHigh) {
			this.dayHigh = dayHigh;
			return this;
		}

		public TradeContextBuilder withDayLow(Double dayLow) {
			this.dayLow = dayLow;
			return this;
		}

		public TradeContextBuilder withAdr(Double adr) {
			this.adr = adr;
			return this;
		}

		public TradeContext build() {
			return new TradeContext(ask, bid, price, dayHigh, dayLow, adr);
		}
	}
}
