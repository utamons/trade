package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

@SuppressWarnings("unused")
public class MoneyStateDTO {
	private final Double capital;
	private final Double profit;

	public MoneyStateDTO(Double capital, Double profit) {
		this.capital = capital;
		this.profit = profit;
	}

	public BigDecimal getCapital() {
		return toOutBigDecimal(capital);
	}

	public BigDecimal getProfit() {
		return toOutBigDecimal(profit);
	}
}
