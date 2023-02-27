package com.corn.trade.dto;

import java.math.BigDecimal;
import java.util.List;

import static com.corn.trade.util.Util.toOutBigDecimal;

@SuppressWarnings("unused")
public class BrokerStatsDTO {
	private final List<CashAccountDTO> accounts;
	private final Double               outcome;
	private final Double               avgOutcome;
	private final Double               avgProfit;
	private final long                 open;
	private final Double               borrowed;

	public BrokerStatsDTO(List<CashAccountDTO> accounts,
	                      Double outcome,
	                      Double avgOutcome,
	                      Double avgProfit,
	                      long open, Double borrowed) {
		this.accounts = accounts;
		this.outcome = outcome;
		this.avgOutcome = avgOutcome;
		this.avgProfit = avgProfit;
		this.open = open;
		this.borrowed = borrowed;
	}

	public List<CashAccountDTO> getAccounts() {
		return accounts;
	}

	public BigDecimal getOutcome() {
		return toOutBigDecimal(outcome);
	}

	public BigDecimal getAvgOutcome() {
		return toOutBigDecimal(avgOutcome);
	}

	public BigDecimal getAvgProfit() {
		return toOutBigDecimal(avgProfit);
	}

	public BigDecimal getBorrowed() {
		return toOutBigDecimal(borrowed);
	}

	public long getOpen() {
		return open;
	}
}
