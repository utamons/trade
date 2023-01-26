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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

	private final CurrencyRateService currencyRateService;

	public CashService(CashAccountRepository accountRepo, CashFlowRepository cashFlowRepo,
	                   BrokerRepository brokerRepo,
	                   CurrencyRepository currencyRepo,
	                   CashAccountTypeRepository accountTypeRepo, CurrencyRateService currencyRateService) {
		this.accountRepo = accountRepo;
		this.cashFlowRepo = cashFlowRepo;
		this.brokerRepo = brokerRepo;
		this.currencyRepo = currencyRepo;
		this.accountTypeRepo = accountTypeRepo;
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

	private CashAccount transfer(BigDecimal transfer,
	                             TradeLog tradeLog,
	                             Broker broker,
	                             Currency currency,
	                             CashAccountType fromType,
	                             CashAccountType toType) {
		CashAccount from    = getAccount(broker, currency, fromType);
		CashAccount to      = getAccount(broker, currency, toType);
		BigDecimal  fromSum = from.getAmount();
		BigDecimal  toSum   = to.getAmount();

		CashFlow record = new CashFlow(from, to, tradeLog, transfer, transfer, null);
		cashFlowRepo.save(record);
		from.setAmount(fromSum.subtract(transfer));
		to.setAmount(toSum.add(transfer));

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
		BigDecimal  transferFrom = exchangeDTO.getAmountFrom();
		BigDecimal  transferTo   = exchangeDTO.getAmountTo();
		BigDecimal  tradeFromSum = tradeFrom.getAmount();
		BigDecimal  tradeToSum   = tradeTo.getAmount();
		BigDecimal  rate         = transferFrom.divide(transferTo, 12, RoundingMode.HALF_DOWN);

		CashFlow record = new CashFlow(tradeFrom, tradeTo, null, transferFrom, transferTo, rate);
		cashFlowRepo.save(record);
		tradeFrom.setAmount(tradeFromSum.subtract(transferFrom));
		tradeTo.setAmount(tradeToSum.add(transferTo));

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

	public void fee(BigDecimal amount, Broker broker, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = currencyRepo.findCurrencyByName("USD");

		transfer(amount, tradeLog, broker, currency, fromTrade, toFee);
		logger.debug("finish");
	}

	public void buy(BigDecimal amount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toOpen    = accountTypeRepo.findCashAccountTypeByName("open");

		transfer(amount, tradeLog, broker, currency, fromTrade, toOpen);
		logger.debug("finish");
	}

	public void sell(BigDecimal openAmount, BigDecimal closeAmount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType tradeType  = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType openType   = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType   = accountTypeRepo.findCashAccountTypeByName("loss");

		transfer(openAmount, tradeLog, broker, currency, openType, tradeType);

		if (closeAmount.compareTo(openAmount) > 0) {
			BigDecimal profit = closeAmount.subtract(openAmount);
			transfer(profit, tradeLog, broker, currency, profitType, tradeType);
		}

		if (closeAmount.compareTo(openAmount) < 0) {
			BigDecimal loss = openAmount.subtract(closeAmount);
			;
			transfer(loss, tradeLog, broker, currency, tradeType, lossType);
		}

		logger.debug("finish");
	}

	public BigDecimal lastDepositAmount(Broker broker, Currency currency) {
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccount     trade     = getAccount(broker, currency, tradeType);
		return trade.getAmount();
	}

	public BigDecimal percentToCapital(BigDecimal outcome,
	                                   BigDecimal openAmount,
	                                   Currency currency) throws JsonProcessingException {
		CashAccountType   tradeType    = accountTypeRepo.findCashAccountTypeByName("trade");
		List<CashAccount> accounts     = accountRepo.findAllByType(tradeType);
		Currency          baseCurrency = currencyRepo.findCurrencyByName("USD");
		BigDecimal        sum          = BigDecimal.ZERO;
		for (CashAccount account : accounts) {
			Currency accCurrency = account.getCurrency();
			if (!accCurrency.getId().equals(baseCurrency.getId())) {
				CurrencyRateDTO currencyRateDTO = currencyRateService.findByDate(accCurrency.getId(), LocalDate.now());
				BigDecimal      convertedSum    =
						account.getAmount().divide(currencyRateDTO.getRate(), 12, RoundingMode.HALF_EVEN);
				sum = sum.add(convertedSum);
			} else {
				sum = sum.add(account.getAmount());
			}
		}

		CurrencyRateDTO currencyRateDTO    = currencyRateService.findByDate(currency.getId(), LocalDate.now());
		BigDecimal      convertedOpenPrice = openAmount.divide(currencyRateDTO.getRate(), 12, RoundingMode.HALF_EVEN);
		sum = sum.add(convertedOpenPrice);

		if (currency.getId().equals(baseCurrency.getId())) {
			return outcome.divide(sum, 12, RoundingMode.HALF_EVEN);
		} else {
			BigDecimal convertedOutcome = outcome.divide(currencyRateDTO.getRate(), 12, RoundingMode.HALF_EVEN);
			return convertedOutcome.divide(sum, 12, RoundingMode.HALF_EVEN);
		}
	}

	public List<CashAccountDTO> getTradeAccounts(Long brokerId) {
		Broker          broker    = brokerRepo.getReferenceById(brokerId);
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		return accountRepo.findAllByBrokerAndType(broker, tradeType)
		                  .stream()
		                  .map(CashAccountMapper::toDTO)
		                  .collect(Collectors.toList());
	}

	public BigDecimal getCapital() throws JsonProcessingException {
		CashAccountType   tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		List<CashAccount> accounts  = accountRepo.findAllByType(tradeType);
		BigDecimal        capital   = BigDecimal.ZERO;
		LocalDate         today     = LocalDate.now();
		for (CashAccount account : accounts) {
			capital = capital.add(currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					account.getAmount(),
					today));
		}

		return capital.setScale(2, RoundingMode.HALF_EVEN);
	}
}
