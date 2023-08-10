package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public record TickerDTO(Long id, String longName, String name, CurrencyDTO currency) implements Serializable {

	@Serial
	private static final long serialVersionUID = 5581176346440601454L;

	@JsonCreator
	public TickerDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("longName") String longName,
			@JsonProperty("name") String name,
			@JsonProperty("currency") CurrencyDTO currency) {
		this.id = id;
		this.longName = longName;
		this.name = name;
		this.currency = currency;
	}


}
