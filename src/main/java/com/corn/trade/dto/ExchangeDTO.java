package com.corn.trade.dto;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class ExchangeDTO {
	private final Long       brokerId;
	private final Long       currencyFromId;
	private final Long       currencyToId;
	private final BigDecimal amountFrom;
	private final BigDecimal amountTo;

	public ExchangeDTO(Long brokerId, Long currencyFromId, Long currencyToId, BigDecimal amountFrom, BigDecimal amountTo) {
		this.brokerId = brokerId;
		this.currencyFromId = currencyFromId;
		this.currencyToId = currencyToId;
		this.amountFrom = amountFrom;
		this.amountTo = amountTo;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getCurrencyFromId() {
		return currencyFromId;
	}

	public Long getCurrencyToId() {
		return currencyToId;
	}

	public BigDecimal getAmountFrom() {
		return amountFrom;
	}

	public BigDecimal getAmountTo() {
		return amountTo;
	}
}
