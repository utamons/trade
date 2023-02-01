package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvalInDTO {
	private final Long brokerId;
	private final Long tickerId;
	private final BigDecimal priceOpen;
	private final Long items;
	private final BigDecimal stopLoss;

	private final LocalDate date;

	@JsonCreator
	public EvalInDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("priceOpen") BigDecimal priceOpen,
			@JsonProperty("items") Long items,
			@JsonProperty("stopLoss") BigDecimal stopLoss,
			@JsonProperty("date") LocalDate date) {
		this.brokerId = brokerId;
		this.tickerId = tickerId;
		this.priceOpen = priceOpen;
		this.items = items;
		this.stopLoss = stopLoss;
		this.date = date;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getTickerId() {
		return tickerId;
	}

	public BigDecimal getPriceOpen() {
		return priceOpen;
	}

	public Long getItems() {
		return items;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public LocalDate getDate() {
		return date;
	}
}
