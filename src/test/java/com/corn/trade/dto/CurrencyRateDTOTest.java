package com.corn.trade.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CurrencyRateDTOTest {

	@Test
	void testGetters() {
		Long id = 1L;
		LocalDate date = LocalDate.now();
		CurrencyDTO currency = new CurrencyDTO(1L, "USD");
		Double rate = 1.0;

		CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(id, date, currency, rate);

		Assertions.assertEquals(id, currencyRateDTO.id());
		Assertions.assertEquals(date, currencyRateDTO.date());
		Assertions.assertEquals(currency, currencyRateDTO.currency());
		Assertions.assertEquals(rate, currencyRateDTO.rate());
	}

	@Test
	void testConstructor() {
		Long id = 1L;
		LocalDate date = LocalDate.now();
		CurrencyDTO currency = new CurrencyDTO(1L, "USD");
		Double rate = 1.0;

		CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(id, date, currency, rate);

		Assertions.assertEquals(id, currencyRateDTO.id());
		Assertions.assertEquals(date, currencyRateDTO.date());
		Assertions.assertEquals(currency, currencyRateDTO.currency());
		Assertions.assertEquals(rate, currencyRateDTO.rate());
	}
}
