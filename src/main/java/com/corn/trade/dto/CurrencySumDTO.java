package com.corn.trade.dto;

public class CurrencySumDTO {
	private final long currencyId;
	private final double sum;

	public CurrencySumDTO(long currencyId, double sum) {
		this.currencyId = currencyId;
		this.sum = sum;
	}

	public long getCurrencyId() {
		return currencyId;
	}

	public double getSum() {
		return sum;
	}
}
