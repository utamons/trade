package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

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
		return toOutBigDecimal(capital);
	}

	public BigDecimal getProfit() {
		return toOutBigDecimal(profit);
	}

	public BigDecimal getRiskBase() {
		return toOutBigDecimal(riskBase);
	}
}
