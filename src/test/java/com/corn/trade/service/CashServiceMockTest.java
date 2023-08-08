package com.corn.trade.service;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.dto.CurrencySumDTO;
import com.corn.trade.dto.EvalInDTO;
import com.corn.trade.dto.EvalOutDTO;
import com.corn.trade.entity.*;
import com.corn.trade.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.corn.trade.service.CashService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CashServiceMockTest {
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
	void setup() {
		currencyRateService = mock(CurrencyRateService.class);
		tradeLogRepo = mock(TradeLogRepository.class);
		accountTypeRepo = mock(CashAccountTypeRepository.class);
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
	void testEstimatedCommission_FreedomFN_KZT() {
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
	void testEstimatedCommission_FreedomFN_USD() {
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
	void testEstimatedCommission_Interactive_USD() {
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

	@ParameterizedTest
	@CsvSource({
			"1, Interactive, USD, 200.04",
			"-1, Interactive, USD, 199.96",
			"1, FreedomFN, USD, 202.32",
			"-1, FreedomFN, USD, 197.71",
			"1, FreedomFN, KZT, 200.38",
			"-1, FreedomFN, KZT, 199.62",

	})
	void testBreakEven(int shortC, String brokerName, String currency, Double result) {
		// Arrange
		CurrencyDTO currencyDTO = new CurrencyDTO(1L, currency);
		long        items       = 50;
		double      priceOpen   = 200.0;

		// Act
		double breakEven = cashService.getBreakEven(shortC, brokerName, currencyDTO, items, priceOpen);

		// Assert
		assertEquals(result, breakEven);
	}

	@Test
	void testGetRiskPc() throws JsonProcessingException {
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
	void testEvalLong() throws JsonProcessingException {
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
	void testEvalShort() throws JsonProcessingException {
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

	@Test
	void testGetOpenPositionsUSD() throws JsonProcessingException {
		// Arrange
		List<CurrencySumDTO> opens = new ArrayList<>();
		opens.add(new CurrencySumDTO(1L, 100.0)); // Assuming currencyId 1 and sum 100.0
		opens.add(new CurrencySumDTO(2L, 200.0)); // Assuming currencyId 2 and sum 200.0
		List<CurrencySumDTO> shortRisks = new ArrayList<>();
		shortRisks.add(new CurrencySumDTO(1L, 10.0)); // Assuming currencyId 1 and sum 10.0
		shortRisks.add(new CurrencySumDTO(2L, 20.0)); // Assuming currencyId 2 and sum 20.0

		// Assume currencyRateService.convertToUSD will return the sum from arguments
		when(currencyRateService.convertToUSD(1L, 100.0, LocalDate.now())).thenReturn(100.0);
		when(currencyRateService.convertToUSD(2L, 200.0, LocalDate.now())).thenReturn(200.0);
		when(currencyRateService.convertToUSD(1L, 10.0, LocalDate.now())).thenReturn(10.0);
		when(currencyRateService.convertToUSD(2L, 20.0, LocalDate.now())).thenReturn(20.0);

		// Assume tradeLogRepo.openLongSums() returns the list of opens
		when(tradeLogRepo.openLongSums()).thenReturn(opens);
		when(tradeLogRepo.openShortRisks()).thenReturn(shortRisks);

		// Act
		double openPositionsUSD = cashService.estimatedPositionsUSD();

		// Assert
		assertEquals(270.0, openPositionsUSD);
	}

	@Test
	void testGetOpenPositionsUSD_EmptyList() throws JsonProcessingException {
		// Arrange
		List<CurrencySumDTO> opens = new ArrayList<>();

		// Assume tradeLogRepo.openLongSums() returns an empty list
		when(tradeLogRepo.openLongSums()).thenReturn(opens);

		// Act
		double openPositionsUSD = cashService.estimatedPositionsUSD();

		// Assert
		assertEquals(0.0, openPositionsUSD);
	}

	@Test
	void testFee_TradeTypeIsNull() {
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", new Currency("USD"));

		when(accountTypeRepo.findCashAccountTypeByName("trade")).thenReturn(null);
		when(accountTypeRepo.findCashAccountTypeByName("fee")).thenReturn(new CashAccountType());

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.fee(100.0, broker, tradeLog, dateTime);
		});

		assertEquals("Trade account type is null", exception.getMessage());
	}

	@Test
	void testFee_FeeTypeIsNull() {
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", new Currency("USD"));

		when(accountTypeRepo.findCashAccountTypeByName("trade")).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName("fee")).thenReturn(null);

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.fee(100.0, broker, tradeLog, dateTime);
		});

		assertEquals("Fee account type is null", exception.getMessage());
	}
	@Test
	void testSellShort_BorrowedTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(BORROWED)).thenReturn(null);

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.sellShort(100, 100.0, 1.0,
			                      dateTime, broker, currency, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testSellShort_OpenTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(null);
		when(accountTypeRepo.findCashAccountTypeByName(BORROWED)).thenReturn(new CashAccountType());

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.sellShort(100, 100.0, 1.0,
			                      dateTime, broker, currency, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testSell_TradeTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(null);
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(new CashAccountType());

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.sell(100, 100.0, 1.0,
			                      dateTime, broker, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testSell_OpenTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(null);
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(new CashAccountType());

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.sell(100, 100.0, 1.0,
			                 dateTime, broker, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testSell_OutcomeTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(null);

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.sell(100, 100.0, 1.0,
			                 dateTime, broker, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testBuy_TradeTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(null);
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(new CashAccountType());

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.buy(100, 100.0, 1.0,
			                 dateTime, broker, currency, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@Test
	void testBuy_OpenTypeIsNull() {
		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog      tradeLog = new TradeLog();
		Broker 	  broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(new CashAccountType());
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(null);

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.buy(100, 100.0, 1.0,
			                dateTime, broker, currency, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}

	@ParameterizedTest
	@CsvSource({"true, false, false, false",
	            "false, true, false, false",
	            "false, false, true, false",
	            "false, false, false, true"})
	void testBuyShort_AccountTypeIsNull(boolean isTradeTypeNull, boolean isBorrowedTypeNull,
	                                  boolean isOpenTypeNull, boolean isOutcomeTypeNull) {

		CashAccountType tradeType = isTradeTypeNull ? null : new CashAccountType();
		CashAccountType borrowedType = isBorrowedTypeNull ? null : new CashAccountType();
		CashAccountType openType = isOpenTypeNull ? null : new CashAccountType();
		CashAccountType outcomeType = isOutcomeTypeNull ? null : new CashAccountType();

		Currency currency = new Currency("USD");
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();
		TradeLog tradeLog = new TradeLog();
		Broker   broker   = new Broker("Test", currency);

		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(tradeType);
		when(accountTypeRepo.findCashAccountTypeByName(BORROWED)).thenReturn(borrowedType);
		when(accountTypeRepo.findCashAccountTypeByName(OPEN)).thenReturn(openType);
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(outcomeType);

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			cashService.buyShort(100, 100.0, 1.0,
			                     1.0, dateTime, broker, tradeLog);
		});

		assertEquals(ACCOUNT_TYPES_ARE_NOT_FOUND, exception.getMessage());
	}
}
