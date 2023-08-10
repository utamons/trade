package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public record CurrencyDTO(Long id, String name) implements Serializable {

	@Serial
	private static final long serialVersionUID = 6328086504066612902L;

	@JsonCreator
	public CurrencyDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name) {
		this.id = id;
		this.name = name;
	}


}
