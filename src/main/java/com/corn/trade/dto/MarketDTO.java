package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@SuppressWarnings("unused")
public class MarketDTO implements Serializable {

	private static final long serialVersionUID = -1651621215447824993L;
	private final        Long id;
	private final String name;

	private final Integer timezone;

	@JsonCreator
	public MarketDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("timezone") Integer timezone) {
		this.id = id;
		this.name = name;
		this.timezone = timezone;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getTimezone() {
		return timezone;
	}
}
