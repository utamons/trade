package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class CurrencyRateDTO {

	private final Long id;

	private final LocalDate date;

	private final CurrencyDTO currency;

	private final Double rate;

	@JsonCreator
	public CurrencyRateDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("date") LocalDate date,
			@JsonProperty("currency") CurrencyDTO currency,
			@JsonProperty("rate") Double rate) {
		this.id = id;
		this.date = date;
		this.currency = currency;
		this.rate = rate;
	}

	public Long getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}

	public Double getRate() {
		return rate;
	}
}
