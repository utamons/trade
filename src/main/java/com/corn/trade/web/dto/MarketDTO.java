package com.corn.trade.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public record MarketDTO(Long id, String name, Integer timezone) implements Serializable {

	@Serial
	private static final long serialVersionUID = -1651621215447824993L;

	@JsonCreator
	public MarketDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("timezone") Integer timezone) {
		this.id = id;
		this.name = name;
		this.timezone = timezone;
	}


}
