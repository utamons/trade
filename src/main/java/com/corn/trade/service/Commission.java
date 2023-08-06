package com.corn.trade.service;

public class Commission {
	private final Double fixed;
	private final Double fly;

	private final Double amount;

	public Commission(Double fixed, Double fly, Double amount) {
		this.fixed = fixed;
		this.fly = fly;
		this.amount = amount;
	}

	public Double getFixed() {
		return fixed;
	}

	public Double getFly() {
		return fly;
	}

	public Double getAmount() {
		return amount;
	}
}
