package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public record BrokerDTO(
		@Serial
		Long id,
		String name,
		CurrencyDTO feeCurrency) implements Serializable {

	@JsonCreator
	public BrokerDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("feeCurrency") CurrencyDTO feeCurrency) {
		this.id = id;
		this.name = name;
		this.feeCurrency = feeCurrency;
	}
}
