package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class TradeLogPageReqDTO {
	private final int pageSize;
	private final int pageNumber;

	@JsonCreator
	public TradeLogPageReqDTO(
			@JsonProperty("pageSize") int pageSize,
			@JsonProperty("pageNumber") int pageNumber) {
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}
}
