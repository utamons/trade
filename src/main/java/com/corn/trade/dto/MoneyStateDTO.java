package com.corn.trade.dto;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class MoneyStateDTO {
	private final BigDecimal capital;
	private final BigDecimal profit;

	public MoneyStateDTO(BigDecimal capital, BigDecimal profit) {
		this.capital = capital;
		this.profit = profit;
	}

	public BigDecimal getCapital() {
		return capital;
	}

	public BigDecimal getProfit() {
		return profit;
	}
}
