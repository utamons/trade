package com.corn.trade.service;

import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.Currency;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.CashAccountRepository;
import com.corn.trade.repository.CashAccountTypeRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.service.CashService.*;
import static com.corn.trade.util.Util.round;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class StatsServiceTest {
	private TradeLogRepository        tradeLogRepo;
	private CashService               cashService;
	private CashAccountTypeRepository accountTypeRepo;
	private CashAccountRepository     cashAccountRepo;
	private CurrencyRateService       currencyRateService;
	private BrokerRepository          brokerRepo;

	private StatsService statsService;

	private Currency usdCurrency, eurCurrency;

	private CashAccountType outcomeType, tradeType;

	@BeforeEach
	void setUp() {
		tradeLogRepo = mock(TradeLogRepository.class);
		cashService = mock(CashService.class);
		accountTypeRepo = mock(CashAccountTypeRepository.class);
		cashAccountRepo = mock(CashAccountRepository.class);
		currencyRateService = mock(CurrencyRateService.class);
		brokerRepo = mock(BrokerRepository.class);
		EntityManager em = mock(EntityManager.class);

		usdCurrency = new Currency(USD);
		usdCurrency.setId(1);
		eurCurrency = new Currency(EUR);
		eurCurrency.setId(2);

		outcomeType = new CashAccountType(OUTCOME, "Outcome");
		tradeType = new CashAccountType(TRADE, "Trading");


		statsService =
				new StatsService(tradeLogRepo, cashService, accountTypeRepo, cashAccountRepo, currencyRateService,
				                 brokerRepo, em);
	}


	@Test
	void testGetMoneyState() throws JsonProcessingException {
		// Arrange

		double capital = 10000.0; // Mocked capital value

		List<CashAccount> accounts = new ArrayList<>(); // Mocked list of accounts
		accounts.add(new CashAccount("USD", usdCurrency, null, outcomeType));
		accounts.add(new CashAccount("EUR", eurCurrency, null, outcomeType));

		when(cashService.getAccountTotal(accounts.get(0))).thenReturn(-1000.0);
		when(cashService.getAccountTotal(accounts.get(1))).thenReturn(-2000.0);
		when(cashService.getCapital(null, null)).thenReturn(capital);
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(outcomeType);
		when(cashAccountRepo.findAllByType(outcomeType)).thenReturn(accounts);
		when(currencyRateService.convertToUSD(eq(usdCurrency.getId()), eq(-1000.0), any())).thenReturn(-1000.0);
		when(currencyRateService.convertToUSD(eq(eurCurrency.getId()), eq(-2000.0), any())).thenReturn(-2196.18);

		// Act
		MoneyStateDTO moneyState = statsService.getMoneyState();

		// Assert
		assertEquals(round(capital), moneyState.getCapital());
		assertEquals(round(31.96), moneyState.getProfit());
	}

	@Test
	void testGetStats() throws JsonProcessingException {
		// Arrange

		double            capital         = 10000.0; // Mocked capital value
		long              brokerId        = 1L;
		Broker            broker          = new Broker("Test Broker", usdCurrency);
		List<CashAccount> outcomeAccounts = new ArrayList<>(); // Mocked list of outcome accounts
		List<CashAccount> tradeAccounts   = new ArrayList<>(); // Mocked list of trade accounts

		outcomeAccounts.add(new CashAccount("USD", usdCurrency, broker, outcomeType));
		outcomeAccounts.add(new CashAccount("EUR", eurCurrency, broker, outcomeType));

		tradeAccounts.add(new CashAccount("USD", usdCurrency, broker, tradeType));
		tradeAccounts.add(new CashAccount("EUR", eurCurrency, broker, tradeType));

		when(cashService.getCapital(broker, null)).thenReturn(capital);
		when(cashService.getRiskBase(capital)).thenReturn(capital);
		when(cashService.getAccountTotal(outcomeAccounts.get(0))).thenReturn(-1000.0);
		when(cashService.getAccountTotal(outcomeAccounts.get(1))).thenReturn(-2000.0);

		when(cashService.getAccountTotal(tradeAccounts.get(0))).thenReturn(1000.0);
		when(cashService.getAccountTotal(tradeAccounts.get(1))).thenReturn(2000.0);

		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(outcomeType);
		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(tradeType);
		when(cashAccountRepo.findAllByBrokerAndType(broker, outcomeType)).thenReturn(outcomeAccounts);
		when(cashAccountRepo.findAllByBrokerAndType(broker, tradeType)).thenReturn(tradeAccounts);
		when(currencyRateService.convertToUSD(eq(usdCurrency.getId()), eq(-1000.0), any())).thenReturn(-1000.0);
		when(currencyRateService.convertToUSD(eq(eurCurrency.getId()), eq(-2000.0), any())).thenReturn(-2196.18);
		when(currencyRateService.convertToUSD(eq(usdCurrency.getId()), eq(1000.0), any())).thenReturn(1000.0);
		when(currencyRateService.convertToUSD(eq(eurCurrency.getId()), eq(2000.0), any())).thenReturn(2196.18);

		when(brokerRepo.getReferenceById(brokerId)).thenReturn(broker);
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(outcomeType);
		when(accountTypeRepo.findCashAccountTypeByName(TRADE)).thenReturn(tradeType);
		when(cashAccountRepo.findAllByBrokerAndType(broker, outcomeType)).thenReturn(outcomeAccounts);
		when(cashAccountRepo.findAllByBrokerAndType(broker, tradeType)).thenReturn(tradeAccounts);

		when(tradeLogRepo.opensCountByBroker(broker)).thenReturn(3L);

		// Act
		BrokerStatsDTO brokerStats = statsService.getBrokerStats(brokerId);

		// Assert
		assertNotNull(brokerStats);
		assertEquals(round(3196.18), brokerStats.getOutcome());
		assertEquals(3, brokerStats.getOpen());
		assertNotNull(brokerStats.getTradeAccounts());
		assertEquals(round(capital), brokerStats.getRiskBase());
		assertEquals(2, brokerStats.getTradeAccounts().size());
		assertEquals(round(1000.0), brokerStats.getTradeAccounts().get(0).amount());
		assertEquals(round(2000.0), brokerStats.getTradeAccounts().get(1).amount());
	}
}
