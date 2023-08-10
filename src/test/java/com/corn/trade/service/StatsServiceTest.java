package com.corn.trade.service;
import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.Currency;
import com.corn.trade.repository.CashAccountRepository;
import com.corn.trade.repository.CashAccountTypeRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.service.CashService.*;
import static com.corn.trade.util.Util.round;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StatsServiceTest {

	@Test
	public void testGetMoneyState() throws JsonProcessingException {
		// Arrange
		TradeLogRepository tradeLogRepo = mock(TradeLogRepository.class);
		CashService               cashService     = mock(CashService.class);
		CashAccountTypeRepository accountTypeRepo     = mock(CashAccountTypeRepository.class);
		CashAccountRepository     cashAccountRepo     = mock(CashAccountRepository.class);
		CurrencyRateService       currencyRateService = mock(CurrencyRateService.class);

		StatsService statsService = new StatsService(tradeLogRepo, cashService, accountTypeRepo, cashAccountRepo, currencyRateService);

		// Mocked data
		Currency          usdCurrency = new Currency(USD);
		usdCurrency.setId(1);
		Currency          eurCurrency = new Currency(EUR);
		eurCurrency.setId(2);
		double            capital     = 10000.0; // Mocked capital value
		CashAccountType   outcomeType = new CashAccountType(OUTCOME,"Outcome");

		List<CashAccount> accounts    = new ArrayList<>(); // Mocked list of accounts
		accounts.add(new CashAccount("USD", usdCurrency, null, outcomeType));
		accounts.add(new CashAccount("EUR", eurCurrency, null, outcomeType));

		when(cashService.getAccountTotal(eq(accounts.get(0)))).thenReturn(-1000.0);
		when(cashService.getAccountTotal(eq(accounts.get(1)))).thenReturn(-2000.0);
		when(cashService.getCapital()).thenReturn(capital);
		when(cashService.getRiskBase(anyDouble())).thenReturn(capital);
		when(accountTypeRepo.findCashAccountTypeByName(OUTCOME)).thenReturn(outcomeType);
		when(cashAccountRepo.findAllByType(outcomeType)).thenReturn(accounts);
		when(currencyRateService.convertToUSD(eq(usdCurrency.getId()), eq(-1000.0), any())).thenReturn(-1000.0);
		when(currencyRateService.convertToUSD(eq(eurCurrency.getId()), eq(-2000.0), any())).thenReturn(-2196.18);

		// Act
		MoneyStateDTO moneyState = statsService.getMoneyState();

		// Assert
		assertEquals(round(capital), moneyState.getCapital());
		assertEquals(round(31.96), moneyState.getProfit());
		assertEquals(round(capital), moneyState.getRiskBase());
	}
}
