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

@SuppressWarnings("unused")
public class ExtendedTradeContext {
	private final Double ask;
	private final Double bid;
	private final Double price;
	private final Double dayHigh;
	private final Double dayLow;
	private final Double adr;
	private final Double slippage;
	private final Double spread;
	private final Double maxRange;
	private final Double maxRangePassedForPos;
	private final Double maxRangeLeftForPos;

	private ExtendedTradeContext(TradeContext tradeContext, Double slippage, Double spread, Double maxRange, Double maxRangePassedForPos, Double maxRangeLeftForPos) {
		this.ask = tradeContext.getAsk();
		this.bid = tradeContext.getBid();
		this.price = tradeContext.getPrice();
		this.dayHigh = tradeContext.getDayHigh();
		this.dayLow = tradeContext.getDayLow();
		this.adr = tradeContext.getAdr();
		this.slippage = slippage;
		this.spread = spread;
		this.maxRange = maxRange;
		this.maxRangePassedForPos = maxRangePassedForPos;
		this.maxRangeLeftForPos = maxRangeLeftForPos;
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

	public Double getSlippage() {
		return slippage;
	}

	public Double getSpread() {
		return spread;
	}

	public Double getMaxRange() {
		return maxRange;
	}

	public Double getMaxRangePassedForPos() {
		return maxRangePassedForPos;
	}

	public Double getMaxRangeLeftForPos() {
		return maxRangeLeftForPos;
	}

	public static final class ExtendedTradeContextBuilder {
		private TradeContext tradeContext;
		private Double slippage;
		private Double spread;
		private Double maxRange;
		private Double maxRangePassedForPos;
		private Double maxRangeLeftForPos;

		private ExtendedTradeContextBuilder() {
		}

		public static ExtendedTradeContextBuilder anExtendedTradeContext() {
			return new ExtendedTradeContextBuilder();
		}

		public ExtendedTradeContextBuilder withTradeContext(TradeContext tradeContext) {
			this.tradeContext = tradeContext;
			return this;
		}

		public ExtendedTradeContextBuilder withSlippage(Double slippage) {
			this.slippage = slippage;
			return this;
		}

		public ExtendedTradeContextBuilder withSpread(Double spread) {
			this.spread = spread;
			return this;
		}

		public ExtendedTradeContextBuilder withMaxRange(Double maxRange) {
			this.maxRange = maxRange;
			return this;
		}

		public ExtendedTradeContextBuilder withMaxRangePassedForPos(Double maxRangePassedForPos) {
			this.maxRangePassedForPos = maxRangePassedForPos;
			return this;
		}

		public ExtendedTradeContextBuilder withMaxRangeLeftForPos(Double maxRangeLeftForPos) {
			this.maxRangeLeftForPos = maxRangeLeftForPos;
			return this;
		}

		public ExtendedTradeContext build() {
			return new ExtendedTradeContext(tradeContext, slippage, spread, maxRange, maxRangePassedForPos, maxRangeLeftForPos);
		}
	}
}
