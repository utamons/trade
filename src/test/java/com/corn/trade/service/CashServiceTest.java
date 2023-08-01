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
	private CashService cashService;

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

	private Currency currencyUSD, currencyEUR;

	private Broker brokerUSD, brokerEUR;

	private CashAccountType savingType, tradeType, correctionType, borrowedType, openType, incomeType, feeType;
	private CashAccount tradeAccountUSD,
			tradeAccountEUR,
			correctionAccount,
			borrowedAccount,
			openAccount,
			incomeAccountUSD,
			feeAccountUSD,
			feeAccountEUR;

	@BeforeEach
	public void setup() {
		entityManager.createQuery("DELETE FROM Broker").executeUpdate();
		entityManager.flush();

		currencyEUR = currencyRepository.getReferenceById(2L);
		currencyUSD = currencyRepository.getReferenceById(1L);

		brokerUSD = new Broker("Test Broker", currencyUSD);
		brokerUSD = brokerRepository.save(brokerUSD);

		brokerEUR = new Broker("Test Broker", currencyEUR);
		brokerEUR = brokerRepository.save(brokerEUR);

		savingType = cashAccountTypeRepository.findCashAccountTypeByName("saving");
		tradeType = cashAccountTypeRepository.findCashAccountTypeByName("trade");
		correctionType = cashAccountTypeRepository.findCashAccountTypeByName("correction");
		borrowedType = cashAccountTypeRepository.findCashAccountTypeByName("borrowed");
		openType = cashAccountTypeRepository.findCashAccountTypeByName("open");
		incomeType = cashAccountTypeRepository.findCashAccountTypeByName("income");
		feeType = cashAccountTypeRepository.findCashAccountTypeByName("fee");

		tradeAccountUSD = new CashAccount("trade/Test Broker/USD", currencyUSD, brokerUSD, tradeType);
		tradeAccountUSD = cashAccountRepository.save(tradeAccountUSD);

		tradeAccountEUR = new CashAccount("trade/Test Broker/EUR", currencyEUR, brokerEUR, tradeType);
		tradeAccountEUR = cashAccountRepository.save(tradeAccountEUR);

		correctionAccount = new CashAccount("correction/Test Broker/USD", currencyUSD, brokerUSD, correctionType);
		correctionAccount = cashAccountRepository.save(correctionAccount);

		borrowedAccount = new CashAccount("borrowed/Test Broker/USD", currencyUSD, brokerUSD, borrowedType);
		borrowedAccount = cashAccountRepository.save(borrowedAccount);

		openAccount = new CashAccount("open/Test Broker/USD", currencyUSD, brokerUSD, openType);
		openAccount = cashAccountRepository.save(openAccount);

		incomeAccountUSD = new CashAccount("income/Test Broker/USD", currencyUSD, brokerUSD, incomeType);
		incomeAccountUSD = cashAccountRepository.save(incomeAccountUSD);

		feeAccountUSD = new CashAccount("fee/Test Broker/USD", currencyUSD, brokerUSD, feeType);
		feeAccountUSD = cashAccountRepository.save(feeAccountUSD);

		feeAccountEUR = new CashAccount("fee/Test Broker/EUR", currencyEUR, brokerEUR, feeType);
		feeAccountEUR = cashAccountRepository.save(feeAccountEUR);
	}

	@Test
	public void testGetAccount_AccountNotFound_CreateNew() {
		// Act
		CashAccount savedAccount =
				cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, savingType);
		assertNull(savedAccount);

		CashAccount result = cashService.getAccount(brokerUSD, currencyUSD, savingType);

		// Assert
		assertNotNull(result);
		assertEquals("saving/Test Broker/USD", result.getName());
		assertEquals(brokerUSD, result.getBroker());
		assertEquals(currencyUSD, result.getCurrency());
		assertEquals(savingType, result.getType());

		savedAccount = cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, savingType);
		assertNotNull(savedAccount);
		assertEquals(result, savedAccount);
	}

	@Test
	public void testGetAccount_AccountFound_ReturnExisting() {
		// Arrange
		CashAccount existingAccount = new CashAccount("TestAccount", currencyUSD, brokerUSD, savingType);
		existingAccount = cashAccountRepository.save(existingAccount);

		// Act
		CashAccount result = cashService.getAccount(brokerUSD, currencyUSD, savingType);

		// Assert
		assertNotNull(result);
		assertEquals(existingAccount, result);
	}

	@Test
	public void testTransfer_Successful_Transfer() {
		// Arrange
		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;

		// Act
		CashAccount result = cashService.transfer(
				transferAmount, null, brokerUSD, currencyUSD, incomeType, tradeType, dateTime
		);

		// Assert
		assertNotNull(result);
		assertEquals(tradeAccountUSD, result);
		assertEquals(-200.0, cashService.getAccountTotal(incomeAccountUSD));
		assertEquals(200.0, cashService.getAccountTotal(tradeAccountUSD));

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(incomeAccountUSD, record.getAccountFrom());
		assertEquals(tradeAccountUSD, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testRefill_Successful_Refill() {
		// Arrange
		TransferDTO transferDTO = new TransferDTO(brokerUSD.getId(), currencyUSD.getId(), 1000.0);

		LocalDateTime dateTime = LocalDateTime.now();
		// Act
		CashAccountDTO result = cashService.refill(transferDTO);

		// Assert
		assertNotNull(result);
		assertEquals(1000.0, cashService.getAccountTotal(result));
		assertEquals("trade/Test Broker/USD", result.name());

		CashAccount fromIncomeAccount =
				accountRepo.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, incomeType);
		CashAccount toTradeAccount =
				accountRepo.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, tradeType);

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
		CashAccount tradeFrom = tradeAccountUSD;
		tradeFrom = cashAccountRepository.save(tradeFrom);

		CashAccount tradeTo = new CashAccount("TradeTo", currencyEUR, brokerUSD, tradeType);
		tradeTo = cashAccountRepository.save(tradeTo);

		ExchangeDTO exchangeDTO = new ExchangeDTO(brokerUSD.getId(), currencyUSD.getId(), currencyEUR.getId(), 800.0,
		                                          700.0);

		LocalDateTime dateTime = LocalDateTime.now();
		// Act
		CashAccountDTO result = cashService.exchange(exchangeDTO);

		// Assert
		assertNotNull(result);
		assertEquals(-800.0, cashService.getAccountTotal(tradeFrom));
		assertEquals(700.0, cashService.getAccountTotal(tradeTo));

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
		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;

		TransferDTO transferDTO = new TransferDTO(brokerUSD.getId(), currencyUSD.getId(), transferAmount);

		// Act
		cashService.correction(transferDTO);

		// Assert
		assertEquals(transferAmount, cashFlowRepository.getSumToByAccount(tradeAccountUSD));
		assertEquals(transferAmount, cashFlowRepository.getSumFromByAccount(correctionAccount));

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(correctionAccount, record.getAccountFrom());
		assertEquals(tradeAccountUSD, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_Negative_Correction() {
		// Arrange
		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = -200.0;

		TransferDTO transferDTO = new TransferDTO(brokerUSD.getId(), currencyUSD.getId(), transferAmount);

		// Act
		cashService.correction(transferDTO);

		// Assert
		assertEquals(-200.0, cashService.getAccountTotal(tradeAccountUSD));
		assertEquals(200.0, cashService.getAccountTotal(correctionAccount));

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(tradeAccountUSD, record.getAccountFrom());
		assertEquals(correctionAccount, record.getAccountTo());
		assertEquals(transferAmount * (-1), record.getSumFrom());
		assertEquals(transferAmount * (-1), record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testCorrection_Successful_CorrectionAccount_Creation() {
		// Arrange
		LocalDateTime dateTime       = LocalDateTime.now();
		double        transferAmount = 200.0;
		cashAccountRepository.delete(correctionAccount);

		TransferDTO transferDTO = new TransferDTO(brokerUSD.getId(), currencyUSD.getId(), transferAmount);

		// Act
		cashService.correction(transferDTO);

		CashAccount correctionAccount =
				cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, correctionType);

		// Assert
		assertNotNull(correctionAccount);
		assertEquals(200.0, cashService.getAccountTotal(tradeAccountUSD));
		assertEquals(-200.0, cashService.getAccountTotal(correctionAccount));

		CashFlow record = cashFlowRepository.findAll().get(0);
		assertNotNull(record);
		assertEquals(correctionAccount, record.getAccountFrom());
		assertEquals(tradeAccountUSD, record.getAccountTo());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testSuccessful_Fee_SameCurrency() {
		// Arrange


		CashFlow toTrade = new CashFlow(incomeAccountUSD, tradeAccountUSD, null, 500.0, 500.0, null, LocalDateTime.now());
		cashFlowRepository.save(toTrade);

		LocalDateTime dateTime = LocalDateTime.now();

		Ticker ticker = tickerRepository.getReferenceById(1L);
		Market market = marketRepository.getReferenceById(1L);

		TradeLog tradeLog = getTradeLog(brokerUSD, ticker, market, currencyUSD);

		double transferAmount = 2.01;
		// Act
		cashService.fee(transferAmount, brokerUSD, tradeLog, dateTime);

		// Assert
		assertEquals(500.0 - transferAmount, cashService.getAccountTotal(tradeAccountUSD));
		assertEquals(transferAmount, cashService.getAccountTotal(feeAccountUSD));

		CashFlow record = cashFlowRepository.findCashFlowByTradeLog(tradeLog);
		assertNotNull(record);
		assertEquals(tradeAccountUSD, record.getAccountFrom());
		assertEquals(feeAccountUSD, record.getAccountTo());
		assertEquals(tradeLog, record.getTradeLog());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testSuccessful_Fee_DifferentCurrency() {
		// Arrange
		CashAccount incomeAccountEUR = new CashAccount("income/Test Broker/EUR", currencyEUR, brokerEUR, incomeType);
		incomeAccountEUR = cashAccountRepository.save(incomeAccountEUR);

		CashFlow toTradeEuro =
				new CashFlow(incomeAccountEUR, tradeAccountEUR, null, 500.0, 500.0, null, LocalDateTime.now());
		cashFlowRepository.save(toTradeEuro);

		LocalDateTime dateTime = LocalDateTime.now();

		Ticker ticker = tickerRepository.getReferenceById(1L);
		Market market = marketRepository.getReferenceById(1L);

		TradeLog tradeLog = getTradeLog(brokerEUR, ticker, market, currencyUSD);

		double transferAmount = 2.01;
		// Act
		cashService.fee(transferAmount, brokerEUR, tradeLog, dateTime);

		// Assert
		assertEquals(500.0 - transferAmount, cashService.getAccountTotal(tradeAccountEUR));
		assertEquals(0.0, cashService.getAccountTotal(tradeAccountUSD));
		assertEquals(transferAmount, cashService.getAccountTotal(feeAccountEUR));

		CashFlow record = cashFlowRepository.findCashFlowByTradeLog(tradeLog);
		assertNotNull(record);
		assertEquals(tradeAccountEUR, record.getAccountFrom());
		assertEquals(feeAccountEUR, record.getAccountTo());
		assertEquals(tradeLog, record.getTradeLog());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	@Test
	public void testSuccessful_Fee_Account_Creation() {
		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();

		Ticker ticker = tickerRepository.getReferenceById(1L);
		Market market = marketRepository.getReferenceById(1L);

		TradeLog tradeLog = getTradeLog(brokerUSD, ticker, market, currencyUSD);

		CashFlow toTrade = new CashFlow(incomeAccountUSD, tradeAccountUSD, null, 500.0, 500.0, null, LocalDateTime.now());
		cashFlowRepository.save(toTrade);

		double transferAmount = 2.01;
		// Act
		cashService.fee(transferAmount, brokerUSD, tradeLog, dateTime);

		CashAccount feeAccount =
				cashAccountRepository.findCashAccountByBrokerAndCurrencyAndType(brokerUSD, currencyUSD, feeType);

		// Assert
		assertNotNull(feeAccount);
		assertEquals("fee/Test Broker/USD", feeAccount.getName());
		assertEquals(500.0 - transferAmount, cashService.getAccountTotal(tradeAccountUSD));
		assertEquals(transferAmount, cashService.getAccountTotal(feeAccount));

		CashFlow record = cashFlowRepository.findCashFlowByTradeLog(tradeLog);
		assertNotNull(record);
		assertEquals(tradeAccountUSD, record.getAccountFrom());
		assertEquals(feeAccount, record.getAccountTo());
		assertEquals(tradeLog, record.getTradeLog());
		assertEquals(transferAmount, record.getSumFrom());
		assertEquals(transferAmount, record.getSumTo());
		assertThat(dateTime).isCloseTo(record.getCommittedAt(), new TemporalUnitWithinOffset(100, ChronoUnit.MILLIS));
	}

	private TradeLog getTradeLog(Broker broker, Ticker ticker, Market market, Currency currency) {
		TradeLog tradeLog = new TradeLog();
		tradeLog.setPosition("long");
		tradeLog.setDateOpen(LocalDateTime.now());
		tradeLog.setBroker(broker);
		tradeLog.setTicker(ticker);
		tradeLog.setMarket(market);
		tradeLog.setCurrency(currency);
		tradeLog.setItemBought(1L);
		tradeLog.setEstimatedPriceOpen(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setOpenStopLoss(1.0);
		tradeLog.setOpenTakeProfit(1.0);
		tradeLog.setLevelPrice(1.0);
		tradeLog.setAtr(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setOpenCommission(1.0);
		tradeLog.setRiskToCapitalPc(1.0);
		tradeLog.setEstimatedFees(1.0);
		tradeLog.setEstimatedBreakEven(1.0);

		tradeLog = tradeLogRepository.save(tradeLog);
		return tradeLog;
	}
}
