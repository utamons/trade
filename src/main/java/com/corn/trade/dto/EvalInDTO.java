package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class EvalInDTO {
	private final Long brokerId;
	private final Long tickerId;
	private final Double priceOpen;
	private final Long items;
	private final Double stopLoss;

	private final LocalDate date;

	private final boolean isShort;

	@JsonCreator
	public EvalInDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("priceOpen") Double priceOpen,
			@JsonProperty("items") Long items,
			@JsonProperty("stopLoss") Double stopLoss,
			@JsonProperty("date") LocalDate date,
			@JsonProperty("short") boolean isShort) {
		this.brokerId = brokerId;
		this.tickerId = tickerId;
		this.priceOpen = priceOpen;
		this.items = items;
		this.stopLoss = stopLoss;
		this.date = date;
		this.isShort = isShort;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getTickerId() {
		return tickerId;
	}

	public Double getPriceOpen() {
		return priceOpen;
	}

	public Long getItems() {
		return items;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public LocalDate getDate() {
		return date;
	}

	public boolean isShort() {
		return isShort;
	}
}
