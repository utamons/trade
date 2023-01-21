package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class SellDTO {
	private final Long brokerId;
	private final Long currencyId;

	private final BigDecimal openAmount;

	private final BigDecimal closeAmount;

	@JsonCreator
	public SellDTO(
			@JsonProperty ("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("openAmount") BigDecimal openAmount,
			@JsonProperty("closeAmount") BigDecimal closeAmount
			) {
		this.brokerId = brokerId;
		this.currencyId = currencyId;
		this.openAmount = openAmount;
		this.closeAmount = closeAmount;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getCurrencyId() {
		return currencyId;
	}

	public BigDecimal getOpenAmount() {
		return openAmount;
	}

	public BigDecimal getCloseAmount() {
		return closeAmount;
	}
}
