package com.corn.trade.service;

import com.corn.trade.dto.*;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.CashAccountMapper;
import com.corn.trade.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@Service
@Transactional
public class CashService {

	private final static Logger logger = LoggerFactory.getLogger(CashService.class);

	private final CashAccountRepository     accountRepo;
	private final CashFlowRepository        cashFlowRepo;
	private final BrokerRepository          brokerRepo;
	private final CurrencyRepository        currencyRepo;
	private final CashAccountTypeRepository accountTypeRepo;
	private final TickerRepository          tickerRepo;
	private final CurrencyRateService       currencyRateService;

	public CashService(CashAccountRepository accountRepo,
	                   CashFlowRepository cashFlowRepo,
	                   BrokerRepository brokerRepo,
	                   CurrencyRepository currencyRepo,
	                   CashAccountTypeRepository accountTypeRepo,
	                   TickerRepository tickerRepo,
	                   CurrencyRateService currencyRateService) {
		this.accountRepo = accountRepo;
		this.cashFlowRepo = cashFlowRepo;
		this.brokerRepo = brokerRepo;
		this.currencyRepo = currencyRepo;
		this.accountTypeRepo = accountTypeRepo;
		this.tickerRepo = tickerRepo;
		this.currencyRateService = currencyRateService;
	}

	private CashAccount getAccount(Broker broker, Currency currency, CashAccountType type) {
		logger.debug("start");
		CashAccount account = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker, currency, type);
		if (account == null) {
			logger.debug("Account '{}' not found for broker {} and currency {}. Creating...",
			             type.getName(), broker.getName(), currency.getName());
			String accountName = type.getName() + "/" + broker.getName() + "/" + currency.getName();
			account = accountRepo.save(new CashAccount(accountName, currency, broker, type));
		} else {
			logger.debug("Found {} account for broker {} and currency {}.",
			             type.getName(), broker.getName(), currency.getName());
		}
		logger.debug("finish");
		return account;
	}

	public CashAccountDTO refill(TransferDTO transferDTO) {
		logger.debug("start");
		Broker          broker     = brokerRepo.getReferenceById(transferDTO.getBrokerId());
		Currency        currency   = currencyRepo.getReferenceById(transferDTO.getCurrencyId());
		CashAccountType toTrade    = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType fromIncome = accountTypeRepo.findCashAccountTypeByName("income");

		CashAccount trade = transfer(transferDTO.getAmount(), null, broker, currency, fromIncome, toTrade);
		logger.debug("finish");
		return CashAccountMapper.toDTO(trade);
	}

	private CashAccount transfer(double transfer,
	                             TradeLog tradeLog,
	                             Broker broker,
	                             Currency currency,
	                             CashAccountType fromType,
	                             CashAccountType toType) {
		CashAccount from    = getAccount(broker, currency, fromType);
		CashAccount to      = getAccount(broker, currency, toType);
		Double      fromSum = from.getAmount();
		Double      toSum   = to.getAmount();

		CashFlow record = new CashFlow(from, to, tradeLog, transfer, transfer, null);
		cashFlowRepo.save(record);
		from.setAmount(fromSum - transfer);
		to.setAmount(toSum + transfer);

		to = accountRepo.save(to);
		accountRepo.save(from);

		cashFlowRepo.flush();
		accountRepo.flush();

		logger.debug("Transfer for amount {}, broker {} and currency {} is finished",
		             transfer, broker.getName(), currency.getName());
		return to;
	}

	public CashAccountDTO exchange(ExchangeDTO exchangeDTO) {
		logger.debug("start");
		Broker          broker       = brokerRepo.getReferenceById(exchangeDTO.getBrokerId());
		Currency        currencyFrom = currencyRepo.getReferenceById(exchangeDTO.getCurrencyFromId());
		Currency        currencyTo   = currencyRepo.getReferenceById(exchangeDTO.getCurrencyToId());
		CashAccountType tradeType    = accountTypeRepo.findCashAccountTypeByName("trade");

		CashAccount tradeFrom    = getAccount(broker, currencyFrom, tradeType);
		CashAccount tradeTo      = getAccount(broker, currencyTo, tradeType);
		double      transferFrom = exchangeDTO.getAmountFrom();
		double      transferTo   = exchangeDTO.getAmountTo();
		double      tradeFromSum = tradeFrom.getAmount();
		double      tradeToSum   = tradeTo.getAmount();
		double      rate         = transferFrom / transferTo;

		CashFlow record = new CashFlow(tradeFrom, tradeTo, null, transferFrom, transferTo, rate);
		cashFlowRepo.save(record);
		tradeFrom.setAmount(tradeFromSum - transferFrom);
		tradeTo.setAmount(tradeToSum + transferTo);

		tradeTo = accountRepo.save(tradeTo);
		accountRepo.save(tradeFrom);

		cashFlowRepo.flush();
		accountRepo.flush();

		logger.debug("Exchange for amount from {}, to {}, broker {} and currency from {}, to {} is finished",
		             transferFrom, transferTo, broker.getName(), currencyFrom.getName(),
		             currencyTo.getName());

		logger.debug("finish");
		return CashAccountMapper.toDTO(tradeTo);
	}

	public void fee(Double amount, Broker broker, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = currencyRepo.findCurrencyByName("USD");

		transfer(amount, tradeLog, broker, currency, fromTrade, toFee);
		logger.debug("finish");
	}

	public void buy(Double amount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toOpen    = accountTypeRepo.findCashAccountTypeByName("open");

		transfer(amount, tradeLog, broker, currency, fromTrade, toOpen);
		logger.debug("finish");
	}

	public void sell(double openAmount, double closeAmount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType tradeType  = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType openType   = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType   = accountTypeRepo.findCashAccountTypeByName("loss");

		transfer(openAmount, tradeLog, broker, currency, openType, tradeType);

		if (closeAmount > openAmount) {
			double profit = closeAmount - openAmount;
			transfer(profit, tradeLog, broker, currency, profitType, tradeType);
		}

		if (closeAmount < openAmount) {
			double loss = openAmount - closeAmount;
			transfer(loss, tradeLog, broker, currency, tradeType, lossType);
		}

		logger.debug("finish");
	}

	public double lastDepositAmount(Broker broker, Currency currency) {
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccount     trade     = getAccount(broker, currency, tradeType);
		return trade.getAmount();
	}

	public double percentToCapital(double outcome,
	                               double openAmount,
	                               Currency currency) throws JsonProcessingException {
		double sum = getCapital() + currencyRateService.convertToUSD(currency.getId(), openAmount, LocalDate.now());
		return currencyRateService.convertToUSD(currency.getId(), outcome, LocalDate.now()) / sum * 100.0;
	}

	public List<CashAccountDTO> getTradeAccounts(Long brokerId) {
		Broker          broker    = brokerRepo.getReferenceById(brokerId);
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		return accountRepo.findAllByBrokerAndType(broker, tradeType)
		                  .stream()
		                  .map(CashAccountMapper::toDTO)
		                  .collect(Collectors.toList());
	}

	public double getCapital() throws JsonProcessingException {
		CashAccountType   tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		List<CashAccount> accounts  = accountRepo.findAllByType(tradeType);
		double            capital   = 0.0;
		LocalDate         today     = LocalDate.now();
		for (CashAccount account : accounts) {
			capital = capital + currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					account.getAmount(),
					today);
		}

		return capital;
	}

	public double getDeposit(long brokerId, LocalDate date) throws JsonProcessingException {
		Broker            broker    = brokerRepo.getReferenceById(brokerId);
		CashAccountType   tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		List<CashAccount> accounts  = accountRepo.findAllByBrokerAndType(broker, tradeType);
		double            deposit   = 0.0;
		for (CashAccount account : accounts) {
			deposit = deposit + currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					account.getAmount(),
					date);
		}

		logger.debug("Deposit for {} on {} = {}", broker.getName(), date, deposit);
		return deposit;
	}

	public EvalOutDTO eval(EvalInDTO evalDTO) throws JsonProcessingException {
		Broker broker     = brokerRepo.getReferenceById(evalDTO.getBrokerId());
		double depositUSD = getDeposit(evalDTO.getBrokerId(), evalDTO.getDate());
		Ticker ticker     = tickerRepo.getReferenceById(evalDTO.getTickerId());

		long items = evalDTO.getItems();

		double fees = getFees(broker, ticker, items, items * evalDTO.getPriceOpen(), evalDTO.getDate());

		double priceUSD =
				currencyRateService.convertToUSD(
						ticker.getCurrency().getId(),
						evalDTO.getPriceOpen(),
						evalDTO.getDate());
		double stopLossUSD =
				currencyRateService.convertToUSD(
						ticker.getCurrency().getId(),
						evalDTO.getStopLoss(),
						evalDTO.getDate());

		double sum = items * priceUSD;

		double sumLoss = items * stopLossUSD;

		double losses = sum - sumLoss;

		double risk = losses/depositUSD*100.0;

		return new EvalOutDTO(fees, risk);
	}

	public Double getFees(Broker broker, Ticker ticker, long items, Double sum, LocalDate date) throws JsonProcessingException {
		double fees = 0.0;
		long currencyId = ticker.getCurrency().getId();

		if (broker.getName().equals("FreedomFN")) {
			if (ticker.getCurrency().getName().equals("KZT")) {
				fees = sum / 100.0 * 0.085;
			} else {
				double fixed = items < 100 ? 1.2 : items * 0.012;
				fees = sum / 100.0 * 0.5 + fixed;
			}
		}
		return currencyRateService.convertToUSD(currencyId, fees, date);
	}
}
