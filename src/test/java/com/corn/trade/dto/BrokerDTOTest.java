package com.corn.trade.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrokerDTOTest {
	@Test
	public void testGetters() {
		BrokerDTO broker = new BrokerDTO(1L, "John Doe");
		assertEquals(1L, broker.getId());
		assertEquals("John Doe", broker.getName());
	}
}
