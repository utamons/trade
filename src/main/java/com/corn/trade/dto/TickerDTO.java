package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@SuppressWarnings("unused")
public class TickerDTO implements Serializable {

	private static final long serialVersionUID = 5581176346440601454L;
	private final Long id;

	private final String      longName;
	private final String      name;
	private final CurrencyDTO currency;

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

	public Long getId() {
		return id;
	}

	public String getLongName() {
		return longName;
	}

	public String getName() {
		return name;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}
}
