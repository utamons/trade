package com.corn.trade.dto;

@SuppressWarnings("unused")
public class ExchangeDTO {
	private final Long       brokerId;
	private final Long       currencyFromId;
	private final Long       currencyToId;
	private final Double amountFrom;
	private final Double amountTo;

	public ExchangeDTO(Long brokerId, Long currencyFromId, Long currencyToId, Double amountFrom, Double amountTo) {
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

	public Double getAmountFrom() {
		return amountFrom;
	}

	public Double getAmountTo() {
		return amountTo;
	}
}
