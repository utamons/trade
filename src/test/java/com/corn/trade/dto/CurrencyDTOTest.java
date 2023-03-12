package com.corn.trade.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CurrencyDTOTest {

	@Test
	void testGetters() {
		Long id = 1L;
		String name = "USD";

		CurrencyDTO currencyDTO = new CurrencyDTO(id, name);

		Assertions.assertEquals(id, currencyDTO.getId());
		Assertions.assertEquals(name, currencyDTO.getName());
	}

	@Test
	void testConstructor() {
		Long id = 1L;
		String name = "USD";

		CurrencyDTO currencyDTO = new CurrencyDTO(id, name);

		Assertions.assertEquals(id, currencyDTO.getId());
		Assertions.assertEquals(name, currencyDTO.getName());
	}

	@Test
	void testJsonCreator() throws JsonProcessingException {
		Long id = 1L;
		String name = "USD";

		CurrencyDTO currencyDTO = new CurrencyDTO(id, name);

		String json = "{\"id\":1,\"name\":\"USD\"}";
		CurrencyDTO deserializedCurrencyDTO = new ObjectMapper().readValue(json, CurrencyDTO.class);

		Assertions.assertEquals(currencyDTO.getId(), deserializedCurrencyDTO.getId());
		Assertions.assertEquals(currencyDTO.getName(), deserializedCurrencyDTO.getName());
	}
}

