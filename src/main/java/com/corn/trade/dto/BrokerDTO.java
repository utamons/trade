package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@SuppressWarnings("unused")
public class BrokerDTO implements Serializable {

	private static final long serialVersionUID = 5157331169613655331L;

	@JsonCreator
	public BrokerDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("name") String name,
			@JsonProperty("shortName") String shortName) {
		this.id = id;
		this.name = name;
		this.shortName = shortName;
	}

	private final Long   id;
	private final String name;
	private final String shortName;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}
}
