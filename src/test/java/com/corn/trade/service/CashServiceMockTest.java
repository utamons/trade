package com.corn.trade.service;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.dto.EvalInDTO;
import com.corn.trade.dto.EvalOutDTO;
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
	private CashService               cashService;
	@Mock
	private CurrencyRateService       currencyRateService;
	@Mock
	private CashAccountRepository     accountRepo;
	@Mock
	private CashFlowRepository        cashFlowRepo;
	@Mock
	private BrokerRepository          brokerRepo;
	@Mock
	private CurrencyRepository        currencyRepo;
	@Mock
	private CashAccountTypeRepository accountTypeRepo;
	@Mock
	private TickerRepository          tickerRepo;
	@Mock
	private TradeLogRepository        tradeLogRepo;

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
	public void testEstimatedCommission_FreedomFN_KZT() throws JsonProcessingException {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, "KZT");
		long        items       = 100;
		Double      sum         = 5000.0;

		// Act
		Commission commission = cashService.estimatedCommission(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(0.0, commission.getFixed());
		assertEquals(4.25, commission.getFly());
		assertEquals(4.25, commission.getAmount());
	}

	@Test
	public void testEstimatedCommission_FreedomFN_USD() throws JsonProcessingException {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		Double      sum         = 200.0;

		// Act
		Commission commission = cashService.estimatedCommission(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(1.2, commission.getFixed());
		assertEquals(1.6, commission.getFly());
		assertEquals(2.8, commission.getAmount());
	}

	@Test
	public void testEstimatedCommission_Interactive_USD() {
		// Arrange
		String      brokerName  = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		Double      sum         = 2000.0;

		// Act
		Commission commission = cashService.estimatedCommission(brokerName, currencyDTO, items, sum);

		// Assert
		assertEquals(0.0, commission.getFixed());
		assertEquals(0.0, commission.getFly());
		assertEquals(1.0, commission.getAmount());
	}

	@Test
	public void testBreakEven_Interactive_USD_Long() {
		// Arrange
		String      brokerName  = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(200.04, breakEven);
	}

	@Test
	public void testBreakEven_Interactive_USD_Short() {
		// Arrange
		String      brokerName  = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(-1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(199.96, breakEven);
	}

	@Test
	public void testBreakEven_Freedom_USD_Long() {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(202.32, breakEven);
	}

	@Test
	public void testBreakEven_Freedom_USD_Short() {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "USD");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(-1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(197.71, breakEven);
	}

	@Test
	public void testBreakEven_Freedom_KZT_Long() {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "KZT");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(200.38, breakEven);
	}

	@Test
	public void testBreakEven_Freedom_KZT_Short() {
		// Arrange
		String      brokerName  = "FreedomFN";
		CurrencyDTO currencyDTO = new CurrencyDTO(2L, "KZT");
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(-1, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(199.62, breakEven);
	}

	@Test
	public void testGetRiskPc() throws JsonProcessingException {
		// Arrange
		long        items       = 100;
		double      stopLoss    = 80.0;
		double      breakEven   = 90.0;
		double      capital     = 10000.0;
		LocalDate   openDate    = LocalDate.now();
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, "USD");

		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 90.0, openDate)).thenReturn(90.0);
		when(currencyRateService.convertToUSD(currencyDTO.getId(), 80.0, openDate)).thenReturn(80.0);

		// Act
		double riskPc = cashService.getRiskPc(items, stopLoss, breakEven, capital, openDate, currencyDTO);

		// Assert
		assertEquals(10.0, riskPc);
	}

	@Test
	public void testEvalLong() throws JsonProcessingException {
		// Arrange
		EvalInDTO dto = new EvalInDTO(
				1L, 1L, 200.0, 10.0, 50L, 198.0, 210.0,
				LocalDate.now(), false
		);
		String      brokerName  = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, "USD");
		double breakEven = cashService.getBreakEven(1, brokerName, currencyDTO, dto.items(), dto.price());

		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), breakEven, dto.date())).thenReturn(breakEven);
		when(currencyRateService.convertToUSD(currencyDTO.getId(), dto.stopLoss(), dto.date())).thenReturn(dto.stopLoss());

		// Act
		EvalOutDTO evalOut = cashService.eval(dto, brokerName, currencyDTO, 100000.0);

		// Assert
		assertEquals(448.0, evalOut.outcomeExp());
		assertEquals(4.48, evalOut.gainPc());
		assertEquals(1.0, evalOut.fees());
		assertEquals(0.1, evalOut.riskPc());
		assertEquals(22.77, evalOut.riskRewardPc());
		assertEquals(200.04, evalOut.breakEven());
		assertEquals(10000.0, evalOut.volume());
	}

	@Test
	public void testEvalShort() throws JsonProcessingException {
		// Arrange
		EvalInDTO dto = new EvalInDTO(
				1L, 1L, 200.0, 10.0, 50L, 202.0, 190.0,
				LocalDate.now(), true
		);
		String      brokerName  = "Interactive";
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, "USD");
		double breakEven = cashService.getBreakEven(-1, brokerName, currencyDTO, dto.items(), dto.price());

		// Assume currencyRateService.convertToUSD will return amount from arguments
		when(currencyRateService.convertToUSD(currencyDTO.getId(), breakEven, dto.date())).thenReturn(breakEven);
		when(currencyRateService.convertToUSD(currencyDTO.getId(), dto.stopLoss(), dto.date())).thenReturn(dto.stopLoss());

		// Act
		EvalOutDTO evalOut = cashService.eval(dto, brokerName, currencyDTO, 100000.0);

		// Assert
		assertEquals(448.0, evalOut.outcomeExp());
		assertEquals(4.48, evalOut.gainPc());
		assertEquals(1.0, evalOut.fees());
		assertEquals(0.1, evalOut.riskPc());
		assertEquals(22.77, evalOut.riskRewardPc());
		assertEquals(199.96, evalOut.breakEven());
		assertEquals(10000.0, evalOut.volume());
	}
}
