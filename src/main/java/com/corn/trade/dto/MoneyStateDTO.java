package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class MoneyStateDTO {
	private final Double capital;
	private final Double profit;

	private final Double riskBase;

	public MoneyStateDTO(Double capital, Double profit, Double riskBase) {
		this.capital = capital;
		this.profit = profit;
		this.riskBase = riskBase;
	}

	public BigDecimal getCapital() {
		return round(capital);
	}

	public BigDecimal getProfit() {
		return round(profit);
	}

	public BigDecimal getRiskBase() {
		return round(riskBase);
	}
}
