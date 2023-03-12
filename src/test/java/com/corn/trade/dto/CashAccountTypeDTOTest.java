package com.corn.trade.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CashAccountTypeDTOTest {

	@Test
	void testGetters() {
		Long id = 1L;
		String name = "Cash Account Type";
		String description = "Description of Cash Account Type";

		CashAccountTypeDTO cashAccountTypeDTO = new CashAccountTypeDTO(id, name, description);

		Assertions.assertEquals(id, cashAccountTypeDTO.getId());
		Assertions.assertEquals(name, cashAccountTypeDTO.getName());
		Assertions.assertEquals(description, cashAccountTypeDTO.getDescription());
	}

	@Test
	void testConstructor() {
		Long id = 1L;
		String name = "Cash Account Type";
		String description = "Description of Cash Account Type";

		CashAccountTypeDTO cashAccountTypeDTO = new CashAccountTypeDTO(id, name, description);

		Assertions.assertEquals(id, cashAccountTypeDTO.getId());
		Assertions.assertEquals(name, cashAccountTypeDTO.getName());
		Assertions.assertEquals(description, cashAccountTypeDTO.getDescription());
	}
}
