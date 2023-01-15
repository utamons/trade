package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@SuppressWarnings("unused")
public class TickerDTO implements Serializable {

	private static final long serialVersionUID = 5581176346440601454L;
	private final Long id;

	private final String name;
	private final String shortName;
	private final CurrencyDTO currency;

    @JsonCreator
	public TickerDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("shortName") String shortName,
			@JsonProperty("currency") CurrencyDTO currency) {
		this.id = id;
		this.name = name;
		this.shortName = shortName;
		this.currency = currency;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}
}
