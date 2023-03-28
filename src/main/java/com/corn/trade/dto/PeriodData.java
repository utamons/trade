package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class PeriodData {
	private final Integer numberOfTrades;
	private final Double volume;
	private final Double profit;
	private final Double performance;
	private final Double rrRatio;
	private final Double capitalSizeAtEndOfPeriod;

	private PeriodData(Integer numberOfTrades,
	                  Double volume,
	                  Double profit,
	                  Double performance,
	                  Double rrRatio,
	                  Double capitalSizeAtEndOfPeriod) {
		this.numberOfTrades = numberOfTrades;
		this.volume = volume;
		this.profit = profit;
		this.performance = performance;
		this.rrRatio = rrRatio;
		this.capitalSizeAtEndOfPeriod = capitalSizeAtEndOfPeriod;
	}



	public Integer getNumberOfTrades() {
		return numberOfTrades;
	}

	public BigDecimal getVolume() {
		return round(volume);
	}

	public BigDecimal getProfit() {
		return round(profit);
	}

	public BigDecimal getPerformance() {
		return round(performance);
	}

	public BigDecimal getRrRatio() {
		return round(rrRatio);
	}

	public BigDecimal getCapitalSizeAtEndOfPeriod() {
		return round(capitalSizeAtEndOfPeriod);
	}

	public static final class PeriodDataBuilder {
		private Integer numberOfTrades;
		private Double  volume;
		private Double  profit;
		private Double  performance;
		private Double  rrRatio;
		private Double  capitalSizeAtEndOfPeriod;

		private PeriodDataBuilder() {
		}

		public static PeriodDataBuilder aPeriodData() {
			return new PeriodDataBuilder();
		}

		public PeriodDataBuilder withNumberOfTrades(Integer numberOfTrades) {
			this.numberOfTrades = numberOfTrades;
			return this;
		}

		public PeriodDataBuilder withVolume(Double volume) {
			this.volume = volume;
			return this;
		}

		public PeriodDataBuilder withProfit(Double profit) {
			this.profit = profit;
			return this;
		}

		public PeriodDataBuilder withPerformance(Double performance) {
			this.performance = performance;
			return this;
		}

		public PeriodDataBuilder withRrRatio(Double rrRatio) {
			this.rrRatio = rrRatio;
			return this;
		}

		public PeriodDataBuilder withCapitalSizeAtEndOfPeriod(Double capitalSizeAtEndOfPeriod) {
			this.capitalSizeAtEndOfPeriod = capitalSizeAtEndOfPeriod;
			return this;
		}

		public PeriodData build() {
			return new PeriodData(numberOfTrades, volume, profit, performance, rrRatio, capitalSizeAtEndOfPeriod);
		}
	}
}
