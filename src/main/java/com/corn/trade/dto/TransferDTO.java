package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransferDTO {
	private final Long brokerId;
	private final Long currencyId;
	private final BigDecimal amount;

	@JsonCreator
	public TransferDTO(
			@JsonProperty ("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("amount") BigDecimal amount) {
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

	public BigDecimal getAmount() {
		return amount;
	}
}
