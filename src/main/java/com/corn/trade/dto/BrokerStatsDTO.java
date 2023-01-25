package com.corn.trade.dto;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("unused")
public class BrokerStatsDTO {
	private final List<CashAccountDTO> accounts;
	private final BigDecimal outcome;
	private final BigDecimal avgOutcome;
	private final BigDecimal avgProfit;
	private final long        open;

	public BrokerStatsDTO(List<CashAccountDTO> accounts,
	                      BigDecimal outcome,
	                      BigDecimal avgOutcome,
	                      BigDecimal avgProfit,
	                      long open) {
		this.accounts = accounts;
		this.outcome = outcome;
		this.avgOutcome = avgOutcome;
		this.avgProfit = avgProfit;
		this.open = open;
	}

	public List<CashAccountDTO> getAccounts() {
		return accounts;
	}

	public BigDecimal getOutcome() {
		return outcome;
	}

	public BigDecimal getAvgOutcome() {
		return avgOutcome;
	}

	public BigDecimal getAvgProfit() {
		return avgProfit;
	}

	public long getOpen() {
		return open;
	}
}
