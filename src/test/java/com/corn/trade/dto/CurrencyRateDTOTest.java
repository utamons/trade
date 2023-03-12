package com.corn.trade.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

		Assertions.assertEquals(id, currencyRateDTO.getId());
		Assertions.assertEquals(date, currencyRateDTO.getDate());
		Assertions.assertEquals(currency, currencyRateDTO.getCurrency());
		Assertions.assertEquals(rate, currencyRateDTO.getRate());
	}

	@Test
	void testConstructor() {
		Long id = 1L;
		LocalDate date = LocalDate.now();
		CurrencyDTO currency = new CurrencyDTO(1L, "USD");
		Double rate = 1.0;

		CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO(id, date, currency, rate);

		Assertions.assertEquals(id, currencyRateDTO.getId());
		Assertions.assertEquals(date, currencyRateDTO.getDate());
		Assertions.assertEquals(currency, currencyRateDTO.getCurrency());
		Assertions.assertEquals(rate, currencyRateDTO.getRate());
	}
}
