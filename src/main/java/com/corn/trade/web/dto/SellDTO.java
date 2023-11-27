package com.corn.trade.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public record SellDTO(Long brokerId, Long currencyId, Double openAmount, Double closeAmount) {
	@JsonCreator
	public SellDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("currencyId") Long currencyId,
			@JsonProperty("openAmount") Double openAmount,
			@JsonProperty("closeAmount") Double closeAmount
	) {
		this.brokerId = brokerId;
		this.currencyId = currencyId;
		this.openAmount = openAmount;
		this.closeAmount = closeAmount;
	}


}
