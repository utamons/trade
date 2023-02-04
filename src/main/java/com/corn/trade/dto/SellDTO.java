package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class SellDTO {
	private final Long brokerId;
	private final Long currencyId;

	private final Double openAmount;

	private final Double closeAmount;

	@JsonCreator
	public SellDTO(
			@JsonProperty ("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("openAmount") Double openAmount,
			@JsonProperty("closeAmount") Double closeAmount
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

	public Double getOpenAmount() {
		return openAmount;
	}

	public Double getCloseAmount() {
		return closeAmount;
	}
}
