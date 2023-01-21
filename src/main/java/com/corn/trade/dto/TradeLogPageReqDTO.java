package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class TradeLogPageReqDTO {
	private final int pageSize;
	private final int skip;

	@JsonCreator
	public TradeLogPageReqDTO(
			@JsonProperty("pageSize") int pageSize,
			@JsonProperty("skip") int skip) {
		this.pageSize = pageSize;
		this.skip = skip;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getSkip() {
		return skip;
	}
}
