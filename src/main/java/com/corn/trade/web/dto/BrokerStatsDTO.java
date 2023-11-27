package com.corn.trade.web.dto;

import java.math.BigDecimal;
import java.util.List;

import static com.corn.trade.web.util.Util.round;

@SuppressWarnings("unused")
public class BrokerStatsDTO {
	private final List<CashAccountOutDTO> tradeAccounts;
	private final Double                  outcome;
	private final long                    open;
	private final Double                  riskBase;

	public BrokerStatsDTO(List<CashAccountOutDTO> tradeAccounts,
	                      Double outcome,
	                      long open, Double riskBase) {
		this.tradeAccounts = tradeAccounts;
		this.outcome = outcome;
		this.open = open;
		this.riskBase = riskBase;
	}

	public List<CashAccountOutDTO> getTradeAccounts() {
		return tradeAccounts;
	}

	public BigDecimal getOutcome() {
		return round(outcome);
	}

	public long getOpen() {
		return open;
	}

	public BigDecimal getRiskBase() {
		return round(riskBase);
	}
}
