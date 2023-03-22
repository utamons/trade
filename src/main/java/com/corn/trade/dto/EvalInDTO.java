package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class EvalInDTO {
	private final Long brokerId;
	private final Long   tickerId;
	private Double price;

	private final Double levelPrice;

	private final Double atr;
	private Long         items;
	private Double stopLoss;

	private final LocalDate date;

	private final boolean isShort;

	@JsonCreator
	public EvalInDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("price") Double price,
			@JsonProperty("levelPrice") Double levelPrice,
			@JsonProperty("atr")  Double atr,
			@JsonProperty("items") Long items,
			@JsonProperty("stopLoss") Double stopLoss,
			@JsonProperty("date") LocalDate date,
			@JsonProperty("short") boolean isShort) {
		this.brokerId = brokerId;
		this.tickerId = tickerId;
		this.price = price;
		this.levelPrice = levelPrice;
		this.atr = atr;
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

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getPrice() {
		return price;
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

	public void setItems(Long items) {
		this.items = items;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getLevelPrice() {
		return levelPrice;
	}

	public Double getAtr() {
		return atr;
	}
}
