package com.corn.trade.trade;

public enum ExchangeType {
	NYSE ("NYSE"),
	NASDAQ ("NASDAQ");

	private final String value;

	ExchangeType(String value) {
		this.value = value;
	}
}
