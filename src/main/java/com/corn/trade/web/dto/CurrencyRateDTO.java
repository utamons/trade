package com.corn.trade.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;


public record CurrencyRateDTO(Long id, LocalDate date, CurrencyDTO currency, Double rate) {

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


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CurrencyRateDTO that))
			return false;
		return id.equals(that.id) && date.equals(that.date) && currency.equals(that.currency) && rate.equals(that.rate);
	}
}
