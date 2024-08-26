package com.corn.trade.model;

public record AccountUpdate(String accountName, String key, String value, String currency) {
	@Override
	public String toString() {
		return "AccountUpdate{" +
				"accountName='" + accountName + '\'' +
				", key='" + key + '\'' +
				", value='" + value + '\'' +
				", currency='" + currency + '\'' +
				'}';
	}
}
