package com.corn.trade.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class CashAccountDTOTest {

	@Test
	void testGetters() {
		Long id = 1L;
		String name = "Cash Account";
		CurrencyDTO currency = new CurrencyDTO(1L,"USD");
		BrokerDTO broker = new BrokerDTO(1L,"Broker");
		String type = "Type";
		Double amount = 100.0;
		LocalDateTime updatedAt = LocalDateTime.now();

		CashAccountDTO cashAccountDTO = new CashAccountDTO(id, name, currency, broker, type, amount, updatedAt);

		Assertions.assertEquals(id, cashAccountDTO.getId());
		Assertions.assertEquals(name, cashAccountDTO.getName());
		Assertions.assertEquals(currency, cashAccountDTO.getCurrency());
		Assertions.assertEquals(broker, cashAccountDTO.getBroker());
		Assertions.assertEquals(type, cashAccountDTO.getType());
		Assertions.assertEquals(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_EVEN), cashAccountDTO.getAmount());
		Assertions.assertEquals(amount, cashAccountDTO.getAmountDouble());
		Assertions.assertEquals(updatedAt, cashAccountDTO.getUpdatedAt());
	}
}

