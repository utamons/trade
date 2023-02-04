package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferDTO {
	private final Long brokerId;
	private final Long currencyId;
	private final Double amount;

	@JsonCreator
	public TransferDTO(
			@JsonProperty ("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("amount") Double amount) {
		this.brokerId = brokerId;
		this.currencyId = currencyId;
		this.amount = amount;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getCurrencyId() {
		return currencyId;
	}

	public Double getAmount() {
		return amount;
	}
}
