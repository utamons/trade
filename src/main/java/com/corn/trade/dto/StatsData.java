package com.corn.trade.dto;

import java.math.BigDecimal;
import java.util.List;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class StatsData {
	private final Integer numberOfTrades;
	private final Integer shorts;
	private final Integer longs;
	private final Double volumeUSD;
	private final Double profitUSD;
	private final Double performance;
	private final Double capitalUsage;
	private final Double profitToCurrentCapital;
	private final Double averageRisk;
	private final Double averageRR;
	private final List<PeriodData> periods;

	private StatsData prevData;

	private StatsData(Integer numberOfTrades,
	                  Integer shorts,
	                  Integer longs,
	                  Double volumeUSD,
	                  Double profitUSD,
	                  Double performance,
	                  Double capitalUsage,
	                  Double profitToCurrentCapital,
	                  Double averageRisk,
	                  Double averageRR,
	                  List<PeriodData> periods,
	                  StatsData prevData) {
		this.numberOfTrades = numberOfTrades;
		this.shorts = shorts;
		this.longs = longs;
		this.volumeUSD = volumeUSD;
		this.profitUSD = profitUSD;
		this.performance = performance;
		this.capitalUsage = capitalUsage;
		this.profitToCurrentCapital = profitToCurrentCapital;
		this.averageRisk = averageRisk;
		this.averageRR = averageRR;
		this.periods = periods;
		this.prevData = prevData;
	}

	public Integer getNumberOfTrades() {
		return numberOfTrades;
	}

	public Integer getShorts() {
		return shorts;
	}

	public Integer getLongs() {
		return longs;
	}

	public BigDecimal getVolumeUSD() {
		return round(volumeUSD);
	}

	public BigDecimal getProfitUSD() {
		return round(profitUSD);
	}

	public BigDecimal getPerformance() {
		return round(performance);
	}

	public BigDecimal getCapitalUsage() {
		return round(capitalUsage);
	}

	public BigDecimal getProfitToCurrentCapital() {
		return round(profitToCurrentCapital);
	}

	public BigDecimal getAverageRisk() {
		return round(averageRisk);
	}

	public BigDecimal getAverageRR() {
		return round(averageRR);
	}

	public List<PeriodData> getPeriods() {
		return periods;
	}

	public StatsData getPrevData() {
		return prevData;
	}

	public void setPrevData(StatsData prevData) {
		this.prevData = prevData;
	}

	@SuppressWarnings("unused")
	public static final class StatsDataBuilder {
		private Integer          numberOfTrades;
		private Integer          shorts;
		private Integer          longs;
		private Double           volumeUSD;
		private Double           profitUSD;
		private Double           performance;
		private Double           capitalUsage;
		private Double           profitToCurrentCapital;
		private Double           averageRisk;
		private Double           averageRR;
		private List<PeriodData> periods;
		private StatsData        prevData;

		private StatsDataBuilder() {
		}

		public static StatsDataBuilder aStatsData() {
			return new StatsDataBuilder();
		}

		public StatsDataBuilder withNumberOfTrades(Integer numberOfTrades) {
			this.numberOfTrades = numberOfTrades;
			return this;
		}

		public StatsDataBuilder withShorts(Integer shorts) {
			this.shorts = shorts;
			return this;
		}

		public StatsDataBuilder withLongs(Integer longs) {
			this.longs = longs;
			return this;
		}

		public StatsDataBuilder withVolumeUSD(Double volumeUSD) {
			this.volumeUSD = volumeUSD;
			return this;
		}

		public StatsDataBuilder withProfitUSD(Double profitUSD) {
			this.profitUSD = profitUSD;
			return this;
		}

		public StatsDataBuilder withPerformance(Double performance) {
			this.performance = performance;
			return this;
		}

		public StatsDataBuilder withCapitalUsage(Double capitalUsage) {
			this.capitalUsage = capitalUsage;
			return this;
		}

		public StatsDataBuilder withProfitToCurrentCapital(Double profitToCurrentCapital) {
			this.profitToCurrentCapital = profitToCurrentCapital;
			return this;
		}

		public StatsDataBuilder withAverageRisk(Double averageRisk) {
			this.averageRisk = averageRisk;
			return this;
		}

		public StatsDataBuilder withAverageRR(Double averageRR) {
			this.averageRR = averageRR;
			return this;
		}

		public StatsDataBuilder withPeriods(List<PeriodData> periods) {
			this.periods = periods;
			return this;
		}

		public StatsDataBuilder withPrevData(StatsData prevData) {
			this.prevData = prevData;
			return this;
		}

		public StatsData build() {
			return new StatsData(numberOfTrades,
			                     shorts,
			                     longs,
			                     volumeUSD,
			                     profitUSD,
			                     performance,
			                     capitalUsage,
			                     profitToCurrentCapital,
			                     averageRisk,
			                     averageRR,
			                     periods,
			                     prevData);
		}
	}
}
