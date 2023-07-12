package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record EvalInDTO(Long brokerId, Long tickerId, Double price, Double atr, Long items, Double stopLoss,
                        Double takeProfit, LocalDate date, boolean isShort) {
	@JsonCreator
	public EvalInDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("price") Double price,
			@JsonProperty("atr") Double atr,
			@JsonProperty("items") Long items,
			@JsonProperty("stopLoss") Double stopLoss,
			@JsonProperty("takeProfit") Double takeProfit,
			@JsonProperty("date") LocalDate date,
			@JsonProperty("short") boolean isShort) {
		this.brokerId = brokerId;
		this.tickerId = tickerId;
		this.price = price;
		this.takeProfit = takeProfit;
		this.atr = atr;
		this.items = items;
		this.stopLoss = stopLoss;
		this.date = date;
		this.isShort = isShort;
	}
}
