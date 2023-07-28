package com.corn.trade.service;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.ExchangeDTO;
import com.corn.trade.dto.TransferDTO;
import com.corn.trade.entity.*;
import com.corn.trade.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles(value = "test")
public class CashServiceTest {

	@Autowired
	private CashAccountRepository cashAccountRepository;

	@Autowired
	private BrokerRepository brokerRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private CashAccountTypeRepository cashAccountTypeRepository;


	@Autowired
	private CashService cashAccountService;

	@Autowired
	private CashFlowRepository cashFlowRepository;

	@Autowired
	private CashAccountRepository accountRepo;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	public void setup() {
		entityManager.createQuery("DELETE FROM Broker").executeUpdate();
		entityManager.createQuery("DELETE FROM CashAccountType").executeUpdate();
		entityManager.flush();
	}

	@Test
	public void testGetAccount_AccountNotFound_CreateNew() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType type = new CashAccountType("Savings", "Savings Account");
		type = cashAccountTypeRepository.save(type);

		// Act
		CashAccount result = cashAccountService.getAccount(broker, currency, type);

		// Assert
		assertNotNull(result);
		assertEquals("Savings/Test Broker/USD", result.getName());
		assertEquals(broker, result.getBroker());
		assertEquals(currency, result.getCurrency());
		assertEquals(type, result.getType());
	}

	@Test
	public void testGetAccount_AccountFound_ReturnExisting() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType type = new CashAccountType("Savings", "Savings Account");
		type = cashAccountTypeRepository.save(type);

		CashAccount existingAccount = new CashAccount("TestAccount", currency, broker, type);
		existingAccount = cashAccountRepository.save(existingAccount);

		// Act
		CashAccount result = cashAccountService.getAccount(broker, currency, type);

		// Assert
		assertNotNull(result);
		assertEquals(existingAccount, result);
	}

	@Test
	public void testTransfer_SuccessfulTransfer() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType fromType = new CashAccountType("FromType", "From Account");
		fromType = cashAccountTypeRepository.save(fromType);

		CashAccountType toType = new CashAccountType("ToType", "To Account");
		toType = cashAccountTypeRepository.save(toType);

		CashAccount fromAccount = new CashAccount("FromAccount", currency, broker, fromType);
		fromAccount.setAmount(1000.0);
		fromAccount = cashAccountRepository.save(fromAccount);

		CashAccount toAccount = new CashAccount("ToAccount", currency, broker, toType);
		toAccount.setAmount(500.0);
		toAccount = cashAccountRepository.save(toAccount);

		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;

		// Act
		CashAccount result = cashAccountService.transfer(
				transferAmount, null, broker, currency, fromType, toType, dateTime
		);

		// Assert
		assertNotNull(result);
		assertEquals(toAccount, result);
		assertEquals(800.0, fromAccount.getAmount());
		assertEquals(700.0, toAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(fromAccount, record.getAccountFrom());
		assertEquals(toAccount, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertEquals(dateTime, record.getCommittedAt());
	}

	@Test
	public void testRefill_SuccessfulRefill() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType fromIncome = new CashAccountType("income", "Income");
		cashAccountTypeRepository.save(fromIncome);

		CashAccountType toTrade = new CashAccountType("trade", "Trade");
		cashAccountTypeRepository.save(toTrade);

		TransferDTO transferDTO = new TransferDTO(broker.getId(), currency.getId(), 1000.0);

		// Act
		CashAccountDTO result = cashAccountService.refill(transferDTO);

		// Assert
		assertNotNull(result);
		assertEquals(1000.0, result.getAmountDouble());
		assertEquals("trade/Test Broker/USD", result.getName());

		CashAccount fromIncomeAccount = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker, currency, fromIncome);
		CashAccount toTradeAccount   = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker, currency, toTrade);

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(fromIncomeAccount, record.getAccountFrom());
		assertEquals(toTradeAccount, record.getAccountTo());
		assertEquals(1000.0, record.getSumFrom());
		assertEquals(1000.0, record.getSumTo());
	}

	@Test
	public void testExchange_SuccessfulExchange() {
		// Arrange

		Currency currencyFrom = currencyRepository.getReferenceById(1L);
		Broker   broker       = new Broker("Test Broker", currencyFrom);
		broker = brokerRepository.save(broker);

		Currency currencyTo = currencyRepository.getReferenceById(2L);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		cashAccountTypeRepository.save(tradeType);

		CashAccount tradeFrom = new CashAccount("TradeFrom", currencyFrom, broker, tradeType);
		tradeFrom.setAmount(1000.0);
		tradeFrom = cashAccountRepository.save(tradeFrom);

		CashAccount tradeTo = new CashAccount("TradeTo", currencyTo, broker, tradeType);
		tradeTo.setAmount(500.0);
		tradeTo = cashAccountRepository.save(tradeTo);

		ExchangeDTO exchangeDTO = new ExchangeDTO(broker.getId(), currencyFrom.getId(), currencyTo.getId(), 800.0, 700.0);

		// Act
		CashAccountDTO result = cashAccountService.exchange(exchangeDTO);

		// Assert
		assertNotNull(result);
		assertEquals(200.0, tradeFrom.getAmount());
		assertEquals(1200.0, tradeTo.getAmount());

		// Verify the CashFlow record is added
		CashFlow cashFlow = cashFlowRepository.findAll().get(0);
		assertNotNull(cashFlow);
		assertNull(cashFlow.getTradeLog());
		assertEquals(tradeFrom, cashFlow.getAccountFrom());
		assertEquals(tradeTo, cashFlow.getAccountTo());
		assertEquals(exchangeDTO.getAmountFrom(), cashFlow.getSumFrom());
		assertEquals(exchangeDTO.getAmountTo(), cashFlow.getSumTo());

		// Verify the CashFlow rate calculation
		double rate = exchangeDTO.getAmountFrom() / exchangeDTO.getAmountTo();
		assertEquals(rate, cashFlow.getExchangeRate());
	}
}
