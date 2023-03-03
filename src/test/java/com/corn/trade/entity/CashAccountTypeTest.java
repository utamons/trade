package com.corn.trade.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CashAccountTypeTest {

	@Test
	public void testGettersAndSetters() {
		// Setup
		CashAccountType accountType = new CashAccountType();
		Long id = 1L;
		String name = "Type A";
		String description = "Type A description";

		// Set values using setters
		accountType.setId(id);
		accountType.setName(name);
		accountType.setDescription(description);

		// Verify using getters
		Assertions.assertEquals(id, accountType.getId());
		Assertions.assertEquals(name, accountType.getName());
		Assertions.assertEquals(description, accountType.getDescription());
	}

	@Test
	public void testConstructor() {
		// Setup
		String name = "Type A";
		String description = "Type A description";

		// Create object using constructor
		CashAccountType accountType = new CashAccountType(name, description);

		// Verify using getters
		Assertions.assertEquals(name, accountType.getName());
		Assertions.assertEquals(description, accountType.getDescription());
	}
}

