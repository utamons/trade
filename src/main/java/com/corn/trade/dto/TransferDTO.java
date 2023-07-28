package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TransferDTO(Long brokerId, Long currencyId, Double amount) {
	@JsonCreator
	public TransferDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("amount") Double amount) {
		this.brokerId = brokerId;
		this.currencyId = currencyId;
		this.amount = amount;
	}
}
