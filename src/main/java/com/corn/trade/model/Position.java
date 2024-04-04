package com.corn.trade.model;

import java.util.Objects;

@SuppressWarnings("unused")
public class Position {
	private final String symbol;
	private final long quantity;
	private final Double averagePrice;
	private final Double marketValue;
	private final Double unrealizedPnl;
	private final Double realizedPnl;

	private Position(String symbol, long quantity, Double averagePrice, Double marketValue, Double unrealizedPnl, Double realizedPnl) {
		this.symbol = symbol;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.marketValue = marketValue;
		this.unrealizedPnl = unrealizedPnl;
		this.realizedPnl = realizedPnl;
	}

	public String getSymbol() {
		return symbol;
	}

	public long getQuantity() {
		return quantity;
	}

	public Double getAveragePrice() {
		return averagePrice;
	}

	public Double getMarketValue() {
		return marketValue;
	}

	public Double getUnrealizedPnl() {
		return unrealizedPnl;
	}

	public Double getRealizedPnl() {
		return realizedPnl;
	}

	public static Position.Builder aPosition() {
		return new Position.Builder();
	}

	public Position.Builder copy() {
		return new Position.Builder(this);
	}


	public static final class Builder {
		private String symbol;
		private long    quantity;
		private Double averagePrice;
		private Double marketValue;
		private Double unrealizedPnl;
		private Double realizedPnl;

		public Builder() {
		}

		private Builder(Position other) {
			this.symbol = other.symbol;
			this.quantity = other.quantity;
			this.averagePrice = other.averagePrice;
			this.marketValue = other.marketValue;
			this.unrealizedPnl = other.unrealizedPnl;
			this.realizedPnl = other.realizedPnl;
		}

		public Builder withSymbol(String symbol) {
			this.symbol = symbol;
			return this;
		}

		public Builder withQuantity(long quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder withAveragePrice(Double averagePrice) {
			this.averagePrice = averagePrice;
			return this;
		}

		public Builder withMarketValue(Double marketValue) {
			this.marketValue = marketValue;
			return this;
		}

		public Builder withUnrealizedPnl(Double unrealizedPnl) {
			this.unrealizedPnl = unrealizedPnl;
			return this;
		}

		public Builder withRealizedPnl(Double realizedPnl) {
			this.realizedPnl = realizedPnl;
			return this;
		}

		public Position build() {
			return new Position(symbol, quantity, averagePrice, marketValue, unrealizedPnl, realizedPnl);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(symbol, quantity, averagePrice, marketValue, unrealizedPnl, realizedPnl);
	}

	@Override
	public String toString() {
		return "Position{" +
				"symbol='" + symbol + '\'' +
				", quantity=" + quantity +
				", averagePrice=" + averagePrice +
				", marketValue=" + marketValue +
				", unrealizedPnl=" + unrealizedPnl +
				", realizedPnl=" + realizedPnl +
				'}';
	}
}
