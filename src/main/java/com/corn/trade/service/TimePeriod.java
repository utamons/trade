package com.corn.trade.service;

public enum TimePeriod {
	CURRENT_WEEK("Current week"),
	LAST_WEEK("Last week"),
	CURRENT_MONTH("Current month"),
	LAST_MONTH("Last month"),
	CURRENT_QUARTER("Current quarter"),
	LAST_QUARTER("Last quarter"),
	CURRENT_YEAR("Current year"),
	LAST_YEAR("Last year");

	private final String description;

	TimePeriod(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}

