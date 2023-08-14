package com.corn.trade.service;

public enum TimePeriod {
	WEEK_TO_DATE("Week to date"),
	LAST_WEEK("Last week"),
	MONTH_TO_DATE("Month to date"),
	LAST_MONTH("Last month"),
	QUARTER_TO_DATE("Quarter to date"),
	LAST_QUARTER("Last quarter"),
	YEAR_TO_DATE("Year to date"),
	LAST_YEAR("Last year");

	private final String description;

	TimePeriod(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}

