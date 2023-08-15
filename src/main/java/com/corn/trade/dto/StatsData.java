package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class StatsData {
	// Common
	private Long       trades;
	private BigDecimal dayWithTradesDayRatio;
	private Long       partials;
	private Long       tradesPerDayMax;
	private BigDecimal tradesPerDayAvg;
	// Volume
	private BigDecimal volume;
	private BigDecimal volumePerTradeMax;
	private BigDecimal volumePerTradeAvg;
	private BigDecimal volumePerDayMax;
	private BigDecimal volumePerDayAvg;
	private BigDecimal volumeToCapitalRatio;
	// Commissions
	private BigDecimal commissionsPerTradeAvg;
	private BigDecimal commissions;
	// Profit
	private BigDecimal profit;
	private BigDecimal profitPerTradeAvg;
	private BigDecimal profitPerDayAvg;
	private BigDecimal profitPerTradeMax;
	private BigDecimal profitPartialsAvg;
	private BigDecimal profitSinglesAvg;
	private BigDecimal profitVolumePc; // Profit/Volume
	private BigDecimal profitCapitalPc; // Profit/Capital (at the start of the period)
	// Loss
	private BigDecimal lossPerTradeAvg;
	private BigDecimal lossPerTradeMax;
	// Quality
	private BigDecimal riskRewardRatioAvg;
	private BigDecimal riskRewardRatioMax;
	private BigDecimal winRate; // Trades with profit / Total trades
	private BigDecimal slippageAvg; // Open slippage
	private BigDecimal takeDeltaAvg; // TakeProfit estimate - TakeProfit real (for positive trades)
	private BigDecimal stopDeltaAvg; // StopLoss real - StopLoss estimate (for negative trades)
	// Money
	private BigDecimal capital;
	private BigDecimal deposit;
	private BigDecimal refills;
	private BigDecimal withdrawals;
	private BigDecimal capitalChange; // Capital - Capital at the start of the period

	private StatsData() {
	}

	public static StatsDataBuilder aStatsData() {
		return new StatsDataBuilder();
	}

	public Long getTrades() {
		return trades;
	}

	public BigDecimal getDayWithTradesDayRatio() {
		return dayWithTradesDayRatio;
	}

	public Long getPartials() {
		return partials;
	}

	public Long getTradesPerDayMax() {
		return tradesPerDayMax;
	}

	public BigDecimal getTradesPerDayAvg() {
		return tradesPerDayAvg;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getVolumePerTradeMax() {
		return volumePerTradeMax;
	}

	public BigDecimal getVolumePerTradeAvg() {
		return volumePerTradeAvg;
	}

	public BigDecimal getVolumePerDayMax() {
		return volumePerDayMax;
	}

	public BigDecimal getVolumePerDayAvg() {
		return volumePerDayAvg;
	}

	public BigDecimal getVolumeToCapitalRatio() {
		return volumeToCapitalRatio;
	}

	public BigDecimal getCommissionsPerTradeAvg() {
		return commissionsPerTradeAvg;
	}

	public BigDecimal getCommissions() {
		return commissions;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public BigDecimal getProfitPerTradeAvg() {
		return profitPerTradeAvg;
	}

	public BigDecimal getProfitPerDayAvg() {
		return profitPerDayAvg;
	}

	public BigDecimal getProfitPerTradeMax() {
		return profitPerTradeMax;
	}

	public BigDecimal getProfitPartialsAvg() {
		return profitPartialsAvg;
	}

	public BigDecimal getProfitSinglesAvg() {
		return profitSinglesAvg;
	}

	public BigDecimal getProfitVolumePc() {
		return profitVolumePc;
	}

	public BigDecimal getProfitCapitalPc() {
		return profitCapitalPc;
	}

	public BigDecimal getLossPerTradeAvg() {
		return lossPerTradeAvg;
	}

	public BigDecimal getLossPerTradeMax() {
		return lossPerTradeMax;
	}

	public BigDecimal getRiskRewardRatioAvg() {
		return riskRewardRatioAvg;
	}

	public BigDecimal getRiskRewardRatioMax() {
		return riskRewardRatioMax;
	}

	public BigDecimal getWinRate() {
		return winRate;
	}

	public BigDecimal getSlippageAvg() {
		return slippageAvg;
	}

	public BigDecimal getTakeDeltaAvg() {
		return takeDeltaAvg;
	}

	public BigDecimal getStopDeltaAvg() {
		return stopDeltaAvg;
	}

	public BigDecimal getCapital() {
		return capital;
	}

	public BigDecimal getDeposit() {
		return deposit;
	}

	public BigDecimal getRefills() {
		return refills;
	}

	public BigDecimal getWithdrawals() {
		return withdrawals;
	}

	public BigDecimal getCapitalChange() {
		return capitalChange;
	}

	public static final class StatsDataBuilder {
		private Long       trades;
		private BigDecimal dayWithTradesDayRatio;
		private Long       partials;
		private Long       tradesPerDayMax;
		private BigDecimal       tradesPerDayAvg;
		private BigDecimal volume;
		private BigDecimal volumePerTradeMax;
		private BigDecimal volumePerTradeAvg;
		private BigDecimal volumePerDayMax;
		private BigDecimal volumePerDayAvg;
		private BigDecimal volumeToCapitalRatio;
		private BigDecimal commissionsPerTradeAvg;
		private BigDecimal commissions;
		private BigDecimal profit;
		private BigDecimal profitPerTradeAvg;
		private BigDecimal profitPerDayAvg;
		private BigDecimal profitPerTradeMax;
		private BigDecimal profitPartialsAvg;
		private BigDecimal profitSinglesAvg;
		private BigDecimal profitVolumePc;
		private BigDecimal profitCapitalPc;
		private BigDecimal lossPerTradeAvg;
		private BigDecimal lossPerTradeMax;
		private BigDecimal riskRewardRatioAvg;
		private BigDecimal riskRewardRatioMax;
		private BigDecimal winRate;
		private BigDecimal slippageAvg;
		private BigDecimal takeDeltaAvg;
		private BigDecimal stopDeltaAvg;
		private BigDecimal capital;
		private BigDecimal deposit;
		private BigDecimal refills;
		private BigDecimal withdrawals;
		private BigDecimal capitalChange;

		private StatsDataBuilder() {
		}

		public StatsDataBuilder withTrades(Long trades) {
			this.trades = trades;
			return this;
		}

		public StatsDataBuilder withDayWithTradesDayRatio(BigDecimal dayWithTradesDayRatio) {
			this.dayWithTradesDayRatio = dayWithTradesDayRatio;
			return this;
		}

		public StatsDataBuilder withPartials(Long partials) {
			this.partials = partials;
			return this;
		}

		public StatsDataBuilder withTradesPerDayMax(Long tradesPerDayMax) {
			this.tradesPerDayMax = tradesPerDayMax;
			return this;
		}

		public StatsDataBuilder withTradesPerDayAvg(BigDecimal tradesPerDayAvg) {
			this.tradesPerDayAvg = tradesPerDayAvg;
			return this;
		}

		public StatsDataBuilder withVolume(BigDecimal volume) {
			this.volume = volume;
			return this;
		}

		public StatsDataBuilder withVolumePerTradeMax(BigDecimal volumePerTradeMax) {
			this.volumePerTradeMax = volumePerTradeMax;
			return this;
		}

		public StatsDataBuilder withVolumePerTradeAvg(BigDecimal volumePerTradeAvg) {
			this.volumePerTradeAvg = volumePerTradeAvg;
			return this;
		}

		public StatsDataBuilder withVolumePerDayMax(BigDecimal volumePerDayMax) {
			this.volumePerDayMax = volumePerDayMax;
			return this;
		}

		public StatsDataBuilder withVolumePerDayAvg(BigDecimal volumePerDayAvg) {
			this.volumePerDayAvg = volumePerDayAvg;
			return this;
		}

		public StatsDataBuilder withVolumeToCapitalRatio(BigDecimal volumeToCapitalRatio) {
			this.volumeToCapitalRatio = volumeToCapitalRatio;
			return this;
		}

		public StatsDataBuilder withCommissionsPerTradeAvg(BigDecimal commissionsPerTradeAvg) {
			this.commissionsPerTradeAvg = commissionsPerTradeAvg;
			return this;
		}

		public StatsDataBuilder withCommissions(BigDecimal commissions) {
			this.commissions = commissions;
			return this;
		}

		public StatsDataBuilder withProfit(BigDecimal profit) {
			this.profit = profit;
			return this;
		}

		public StatsDataBuilder withProfitPerTradeAvg(BigDecimal profitPerTradeAvg) {
			this.profitPerTradeAvg = profitPerTradeAvg;
			return this;
		}

		public StatsDataBuilder withProfitPerDayAvg(BigDecimal profitPerDayAvg) {
			this.profitPerDayAvg = profitPerDayAvg;
			return this;
		}

		public StatsDataBuilder withProfitPerTradeMax(BigDecimal profitPerTradeMax) {
			this.profitPerTradeMax = profitPerTradeMax;
			return this;
		}

		public StatsDataBuilder withProfitPartialsAvg(BigDecimal profitPartialsAvg) {
			this.profitPartialsAvg = profitPartialsAvg;
			return this;
		}

		public StatsDataBuilder withProfitSinglesAvg(BigDecimal profitSinglesAvg) {
			this.profitSinglesAvg = profitSinglesAvg;
			return this;
		}

		public StatsDataBuilder withProfitVolumePc(BigDecimal profitVolumePc) {
			this.profitVolumePc = profitVolumePc;
			return this;
		}

		public StatsDataBuilder withProfitCapitalPc(BigDecimal profitCapitalPc) {
			this.profitCapitalPc = profitCapitalPc;
			return this;
		}

		public StatsDataBuilder withLossPerTradeAvg(BigDecimal lossPerTradeAvg) {
			this.lossPerTradeAvg = lossPerTradeAvg;
			return this;
		}

		public StatsDataBuilder withLossPerTradeMax(BigDecimal lossPerTradeMax) {
			this.lossPerTradeMax = lossPerTradeMax;
			return this;
		}

		public StatsDataBuilder withRiskRewardRatioAvg(BigDecimal riskRewardRatioAvg) {
			this.riskRewardRatioAvg = riskRewardRatioAvg;
			return this;
		}

		public StatsDataBuilder withRiskRewardRatioMax(BigDecimal riskRewardRatioMax) {
			this.riskRewardRatioMax = riskRewardRatioMax;
			return this;
		}

		public StatsDataBuilder withWinRate(BigDecimal winRate) {
			this.winRate = winRate;
			return this;
		}

		public StatsDataBuilder withSlippageAvg(BigDecimal slippageAvg) {
			this.slippageAvg = slippageAvg;
			return this;
		}

		public StatsDataBuilder withTakeDeltaAvg(BigDecimal takeDeltaAvg) {
			this.takeDeltaAvg = takeDeltaAvg;
			return this;
		}

		public StatsDataBuilder withStopDeltaAvg(BigDecimal stopDeltaAvg) {
			this.stopDeltaAvg = stopDeltaAvg;
			return this;
		}

		public StatsDataBuilder withCapital(double capital) {
			this.capital = round(capital);
			return this;
		}

		public StatsDataBuilder withDeposit(BigDecimal deposit) {
			this.deposit = deposit;
			return this;
		}

		public StatsDataBuilder withRefills(BigDecimal refills) {
			this.refills = refills;
			return this;
		}

		public StatsDataBuilder withWithdrawals(BigDecimal withdrawals) {
			this.withdrawals = withdrawals;
			return this;
		}

		public StatsDataBuilder withCapitalChange(BigDecimal capitalChange) {
			this.capitalChange = capitalChange;
			return this;
		}

		public StatsData build() {
			StatsData statsData = new StatsData();
			statsData.slippageAvg = this.slippageAvg;
			statsData.deposit = this.deposit;
			statsData.profitPerTradeAvg = this.profitPerTradeAvg;
			statsData.lossPerTradeMax = this.lossPerTradeMax;
			statsData.takeDeltaAvg = this.takeDeltaAvg;
			statsData.lossPerTradeAvg = this.lossPerTradeAvg;
			statsData.commissionsPerTradeAvg = this.commissionsPerTradeAvg;
			statsData.capitalChange = this.capitalChange;
			statsData.volumePerDayAvg = this.volumePerDayAvg;
			statsData.volumePerTradeAvg = this.volumePerTradeAvg;
			statsData.capital = this.capital;
			statsData.profitSinglesAvg = this.profitSinglesAvg;
			statsData.volumePerTradeMax = this.volumePerTradeMax;
			statsData.riskRewardRatioAvg = this.riskRewardRatioAvg;
			statsData.withdrawals = this.withdrawals;
			statsData.profitVolumePc = this.profitVolumePc;
			statsData.volumePerDayMax = this.volumePerDayMax;
			statsData.commissions = this.commissions;
			statsData.riskRewardRatioMax = this.riskRewardRatioMax;
			statsData.profitCapitalPc = this.profitCapitalPc;
			statsData.profitPerTradeMax = this.profitPerTradeMax;
			statsData.refills = this.refills;
			statsData.winRate = this.winRate;
			statsData.volume = this.volume;
			statsData.profitPerDayAvg = this.profitPerDayAvg;
			statsData.stopDeltaAvg = this.stopDeltaAvg;
			statsData.tradesPerDayAvg = this.tradesPerDayAvg;
			statsData.dayWithTradesDayRatio = this.dayWithTradesDayRatio;
			statsData.volumeToCapitalRatio = this.volumeToCapitalRatio;
			statsData.partials = this.partials;
			statsData.tradesPerDayMax = this.tradesPerDayMax;
			statsData.trades = this.trades;
			statsData.profit = this.profit;
			statsData.profitPartialsAvg = this.profitPartialsAvg;
			return statsData;
		}
	}
}
