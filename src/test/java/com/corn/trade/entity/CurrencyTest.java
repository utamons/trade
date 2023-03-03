package com.corn.trade.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest {

	@Test
	void testSetAndGetId() {
		Currency currency = new Currency();
		currency.setId(1L);
		assertEquals(1L, currency.getId());
	}

	@Test
	void testSetAndGetName() {
		Currency currency = new Currency();
		currency.setName("USD");
		assertEquals("USD", currency.getName());
	}

}
