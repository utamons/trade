package com.corn.trade.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class CurrencyRateTest {

	@Test
	public void testGettersAndSetters() {
		Currency currency = new Currency();
		currency.setName("USD");

		CurrencyRate rate = new CurrencyRate();
		rate.setDate(LocalDate.of(2022, 2, 21));
		rate.setCurrency(currency);
		rate.setRate(1.0);

		assertEquals(LocalDate.of(2022, 2, 21), rate.getDate());
		assertEquals(currency, rate.getCurrency());
		assertEquals(1.0, rate.getRate());
	}
}

