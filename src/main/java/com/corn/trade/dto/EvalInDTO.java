package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

import static com.corn.trade.util.Util.round;

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
		this.price = round(price, 2);
		this.takeProfit = round(takeProfit, 2);
		this.atr = round(atr, 2);
		this.items = items;
		this.stopLoss =round(stopLoss, 2);
		this.date = date;
		this.isShort = isShort;
	}
}
