package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TradeLogPageReqDTO(int pageSize, int pageNumber) {
	@JsonCreator
	public TradeLogPageReqDTO(
			@JsonProperty("pageSize") int pageSize,
			@JsonProperty("pageNumber") int pageNumber) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}
}
