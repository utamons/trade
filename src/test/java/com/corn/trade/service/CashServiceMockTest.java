package com.corn.trade.service;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CashServiceMockTest {
	private CashService cashService;
	@Mock
	private CurrencyRateService currencyRateService;
	@Mock
	private CashAccountRepository accountRepo;
	@Mock
	private CashFlowRepository cashFlowRepo;
	@Mock
	private BrokerRepository brokerRepo;
	@Mock
	private CurrencyRepository currencyRepo;
	@Mock
	private CashAccountTypeRepository accountTypeRepo;
	@Mock
	private TickerRepository tickerRepo;
	@Mock
	private TradeLogRepository tradeLogRepo;

	@BeforeEach
	public void setup() {
		currencyRateService = mock(CurrencyRateService.class);
		cashService = new CashService(
				accountRepo,
				cashFlowRepo,
				brokerRepo,
				currencyRepo,
				accountTypeRepo,
				tickerRepo,
				tradeLogRepo,
				currencyRateService
		);
	}

	@Test
	public void testGetFees_FreedomFN_KZT() throws JsonProcessingException {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, "KZT");
		long        items       = 100;
		Double      sum         = 5000.0;


		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 42.5, LocalDate.now())).thenReturn(42.5);

		// Act
		Fees fees = cashService.getFees(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(0.0, fees.getFixed());
		assertEquals(4.25, fees.getFly());
		assertEquals(42.5, fees.getAmount());
	}

	@Test
	public void testGetFees_FreedomFN_USD() throws JsonProcessingException {
		// Arrange
		String brokerName = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long items = 200;
		Double sum = 15000.0;

		CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 180.0, LocalDate.now())).thenReturn(180.0);

		// Act
		Fees fees = cashService.getFees(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(1.2, fees.getFixed());
		assertEquals(77.4, fees.getFly());
		assertEquals(193.2, fees.getAmount());
	}

	@Test
	public void testGetFees_Interactive_USD() throws JsonProcessingException {
		// Arrange
		String brokerName = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long items = 50;
		Double sum = 2000.0;

		CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 100.0, LocalDate.now())).thenReturn(100.0);

		// Act
		Fees fees = cashService.getFees(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(0.0, fees.getFixed());
		assertEquals(0.0, fees.getFly());
		assertEquals(100.0, fees.getAmount());
	}

	@Test
	public void testGetFees_Interactive_EUR() throws JsonProcessingException {
		// Arrange
		String brokerName = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(3L, "EUR");
		long items = 100;
		Double sum = 8000.0;

		CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 97.6, LocalDate.now())).thenReturn(97.6);

		// Act
		Fees fees = cashService.getFees(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(0.0, fees.getFixed());
		assertEquals(2.4, fees.getFly());
		assertEquals(99.2, fees.getAmount());
	}
}
