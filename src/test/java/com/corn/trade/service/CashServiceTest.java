package com.corn.trade.service;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.ExchangeDTO;
import com.corn.trade.dto.TransferDTO;
import com.corn.trade.entity.*;
import com.corn.trade.repository.*;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Autowired
	private MarketRepository marketRepository;

	@Autowired
	private TradeLogRepository tradeLogRepository;

	@Autowired
	private TickerRepository tickerRepository;

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

		CashAccount savedAccount = cashAccountRepository.findAll().get(0);
		assertNotNull(savedAccount);
		assertEquals(result, savedAccount);
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
	public void testTransfer_Successful_Transfer() {
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
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testRefill_Successful_Refill() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType fromIncome = new CashAccountType("income", "Income");
		cashAccountTypeRepository.save(fromIncome);

		CashAccountType toTrade = new CashAccountType("trade", "Trade");
		cashAccountTypeRepository.save(toTrade);

		TransferDTO transferDTO = new TransferDTO(broker.getId(), currency.getId(), 1000.0);

		LocalDateTime dateTime       = LocalDateTime.now();
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
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testExchange_Successful_Exchange() {
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

		LocalDateTime dateTime       = LocalDateTime.now();
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
		assertThat(dateTime).isCloseTo(cashFlow.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));

		// Verify the CashFlow rate calculation
		double rate = exchangeDTO.getAmountFrom() / exchangeDTO.getAmountTo();
		assertEquals(rate, cashFlow.getExchangeRate());
	}

	@Test
	public void testCorrection_Successful_Positive_Correction() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		tradeType = cashAccountTypeRepository.save(tradeType);

		CashAccountType correctionType = new CashAccountType("correction", "Correction");
		correctionType = cashAccountTypeRepository.save(correctionType);

		CashAccount tradeAccount = new CashAccount("trade/Test Broker/USD", currency, broker, tradeType);
		tradeAccount.setAmount(500.0);
		tradeAccount = cashAccountRepository.save(tradeAccount);

		CashAccount correctionAccount = new CashAccount("correction/Test Broker/USD", currency, broker, correctionType);
		correctionAccount.setAmount(0.0);
		correctionAccount = cashAccountRepository.save(correctionAccount);

		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;

		TransferDTO transferDTO = new TransferDTO(broker.getId(), currency.getId(), transferAmount);

		// Act
		cashAccountService.correction(transferDTO);

		// Assert
		assertEquals(700.0, tradeAccount.getAmount());
		assertEquals(-200.0, correctionAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(correctionAccount, record.getAccountFrom());
		assertEquals(tradeAccount, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_Negative_Correction() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		tradeType = cashAccountTypeRepository.save(tradeType);

		CashAccountType correctionType = new CashAccountType("correction", "Correction");
		correctionType = cashAccountTypeRepository.save(correctionType);

		CashAccount tradeAccount = new CashAccount("trade/Test Broker/USD", currency, broker, tradeType);
		tradeAccount.setAmount(500.0);
		tradeAccount = cashAccountRepository.save(tradeAccount);

		CashAccount correctionAccount = new CashAccount("correction/Test Broker/USD", currency, broker, correctionType);
		correctionAccount.setAmount(0.0);
		correctionAccount = cashAccountRepository.save(correctionAccount);

		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = -200.0;

		TransferDTO transferDTO = new TransferDTO(broker.getId(), currency.getId(), transferAmount);

		// Act
		cashAccountService.correction(transferDTO);

		// Assert
		assertEquals(300.0, tradeAccount.getAmount());
		assertEquals(200.0, correctionAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(tradeAccount, record.getAccountFrom());
		assertEquals(correctionAccount, record.getAccountTo());
		assertEquals(transferAmount * (-1), record.getSumFrom());
		assertEquals(transferAmount * (-1), record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_CorrectionAccount_Creation() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(1L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		tradeType = cashAccountTypeRepository.save(tradeType);

		CashAccountType correctionType = new CashAccountType("correction", "Correction");
		correctionType = cashAccountTypeRepository.save(correctionType);

		CashAccount tradeAccount = new CashAccount("trade/Test Broker/USD", currency, broker, tradeType);
		tradeAccount.setAmount(500.0);
		tradeAccount = cashAccountRepository.save(tradeAccount);

		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;

		TransferDTO transferDTO = new TransferDTO(broker.getId(), currency.getId(), transferAmount);

		// Act
		cashAccountService.correction(transferDTO);

		CashAccount correctionAccount = cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(broker, currency, correctionType);

		// Assert
		assertNotNull(correctionAccount);
		assertEquals(700.0, tradeAccount.getAmount());
		assertEquals(-200.0, correctionAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(correctionAccount, record.getAccountFrom());
		assertEquals(tradeAccount, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_Fee() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(2L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		tradeType = cashAccountTypeRepository.save(tradeType);

		CashAccountType feeType = new CashAccountType("fee", "Fee");
		feeType = cashAccountTypeRepository.save(feeType);

		CashAccount tradeAccount = new CashAccount("trade/Test Broker/EUR", currency, broker, tradeType);
		tradeAccount.setAmount(500.0);
		tradeAccount = cashAccountRepository.save(tradeAccount);

		CashAccount feeAccount = new CashAccount("fee/Test Broker/EUR", currency, broker, feeType);
		feeAccount.setAmount(0.0);
		feeAccount = cashAccountRepository.save(feeAccount);

		LocalDateTime dateTime       = LocalDateTime.now();

		Ticker ticker = tickerRepository.getReferenceById(1L);
		Market market = marketRepository.getReferenceById(1L);

		TradeLog tradeLog = new TradeLog();
		tradeLog.setPosition("long");
		tradeLog.setDateOpen(LocalDateTime.now());
		tradeLog.setBroker(broker);
		tradeLog.setTicker(ticker);
		tradeLog.setMarket(market);
		tradeLog.setCurrency(currency);
		tradeLog.setItemNumber(1L);
		tradeLog.setPriceOpen(1.0);
		tradeLog.setVolume(1.0);
		tradeLog.setVolumeToDeposit(1.0);
		tradeLog.setStopLoss(1.0);
		tradeLog.setTakeProfit(1.0);
		tradeLog.setOutcome(1.0);
		tradeLog.setOutcomeExpected(1.0);
		tradeLog.setLevelPrice(1.0);
		tradeLog.setAtr(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setFees(1.0);

		tradeLog = tradeLogRepository.save(tradeLog);

		double        transferAmount = 2.01;
		// Act
		cashAccountService.fee(transferAmount, broker, tradeLog, dateTime);

		// Assert
		assertEquals(500.0 - transferAmount, tradeAccount.getAmount());
		assertEquals(transferAmount, feeAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(tradeAccount, record.getAccountFrom());
		assertEquals(feeAccount, record.getAccountTo());
		assertEquals(tradeLog, record.getTradeLog());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_Fee_Account_Creation() {
		// Arrange
		Currency currency = currencyRepository.getReferenceById(2L);
		Broker   broker   = new Broker("Test Broker", currency);
		broker = brokerRepository.save(broker);

		CashAccountType tradeType = new CashAccountType("trade", "Trade");
		tradeType = cashAccountTypeRepository.save(tradeType);

		CashAccountType feeType = new CashAccountType("fee", "Fee");
		feeType = cashAccountTypeRepository.save(feeType);

		CashAccount tradeAccount = new CashAccount("trade/Test Broker/EUR", currency, broker, tradeType);
		tradeAccount.setAmount(500.0);
		tradeAccount = cashAccountRepository.save(tradeAccount);

		LocalDateTime dateTime       = LocalDateTime.now();

		Ticker ticker = tickerRepository.getReferenceById(1L);
		Market market = marketRepository.getReferenceById(1L);

		TradeLog tradeLog = new TradeLog();
		tradeLog.setPosition("long");
		tradeLog.setDateOpen(LocalDateTime.now());
		tradeLog.setBroker(broker);
		tradeLog.setTicker(ticker);
		tradeLog.setMarket(market);
		tradeLog.setCurrency(currency);
		tradeLog.setItemNumber(1L);
		tradeLog.setPriceOpen(1.0);
		tradeLog.setVolume(1.0);
		tradeLog.setVolumeToDeposit(1.0);
		tradeLog.setStopLoss(1.0);
		tradeLog.setTakeProfit(1.0);
		tradeLog.setOutcome(1.0);
		tradeLog.setOutcomeExpected(1.0);
		tradeLog.setLevelPrice(1.0);
		tradeLog.setAtr(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setFees(1.0);

		tradeLog = tradeLogRepository.save(tradeLog);

		double        transferAmount = 2.01;
		// Act
		cashAccountService.fee(transferAmount, broker, tradeLog, dateTime);

		CashAccount feeAccount = cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(broker, currency, feeType);

		// Assert
		assertNotNull(feeAccount);
		assertEquals("fee/Test Broker/EUR", feeAccount.getName());
		assertEquals(500.0 - transferAmount, tradeAccount.getAmount());
		assertEquals(transferAmount, feeAccount.getAmount());

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(tradeAccount, record.getAccountFrom());
		assertEquals(feeAccount, record.getAccountTo());
		assertEquals(tradeLog, record.getTradeLog());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}
}
