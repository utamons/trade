package com.corn.trade.service;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.ExchangeDTO;
import com.corn.trade.dto.RefillDTO;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.CashAccountMapper;
import com.corn.trade.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CashService {

	private final static Logger logger = LoggerFactory.getLogger(CashService.class);

	private final CashAccountRepository accountRepo;
	private final CashFlowRepository cashFlowRepo;
	private final BrokerRepository brokerRepo;
	private final CurrencyRepository currencyRepo;
	private final CashAccountTypeRepository accountTypeRepo;

	public CashService(CashAccountRepository accountRepo, CashFlowRepository cashFlowRepo,
	                   BrokerRepository brokerRepo,
	                   CurrencyRepository currencyRepo,
	                   CashAccountTypeRepository accountTypeRepo) {
		this.accountRepo = accountRepo;
		this.cashFlowRepo = cashFlowRepo;
		this.brokerRepo = brokerRepo;
		this.currencyRepo = currencyRepo;
		this.accountTypeRepo = accountTypeRepo;
	}

	private CashAccount getAccount(Broker broker, Currency currency, CashAccountType type) {
		logger.debug("start");
		CashAccount account = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker,currency,type);
		if (account == null) {
			logger.debug("Account '{}' not found for broker {} and currency {}. Creating...",
			             type.getName(), broker.getName(), currency.getName());
			String accountName = type.getName()+"/"+broker.getName()+"/"+currency.getName();
			account = accountRepo.save(new CashAccount(accountName, currency, broker, type));
		} else {
			logger.debug("Found {} account for broker {} and currency {}.",
			             type.getName(), broker.getName(), currency.getName());
		}
		logger.debug("finish");
		return account;
	}

	@Transactional
	public CashAccountDTO refill(RefillDTO refillDTO) {
		logger.debug("start");
		Broker broker = brokerRepo.getReferenceById(refillDTO.getBrokerId());
		Currency currency = currencyRepo.getReferenceById(refillDTO.getCurrencyId());
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType incomeType = accountTypeRepo.findCashAccountTypeByName("income");

		CashAccount trade = getAccount(broker, currency, tradeType);
		CashAccount income = getAccount(broker, currency, incomeType);
		BigDecimal tradeSum = trade.getAmount();
		BigDecimal incomeSum = income.getAmount();
		BigDecimal transfer = refillDTO.getAmount();

		CashFlow record = new CashFlow(income, trade, transfer, transfer, null);
		cashFlowRepo.save(record);
		trade.setAmount(tradeSum.add(transfer));
		income.setAmount(incomeSum.subtract(transfer));

		trade = accountRepo.save(trade);
		accountRepo.save(income);

		cashFlowRepo.flush();
		accountRepo.flush();

		logger.debug("Refill for amount {}, broker {} and currency {} is finished",
		             transfer, broker.getName(), currency.getName() );

		logger.debug("finish");
		return CashAccountMapper.toDTO(trade);
	}

	public CashAccountDTO exchange(ExchangeDTO exchangeDTO) {
		logger.debug("start");
		Broker broker = brokerRepo.getReferenceById(exchangeDTO.getBrokerId());
		Currency currencyFrom = currencyRepo.getReferenceById(exchangeDTO.getCurrencyFromId());
		Currency currencyTo = currencyRepo.getReferenceById(exchangeDTO.getCurrencyToId());
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");

		CashAccount tradeFrom = getAccount(broker, currencyFrom, tradeType);
		CashAccount tradeTo = getAccount(broker, currencyTo, tradeType);
		BigDecimal transferFrom = exchangeDTO.getAmountFrom();
		BigDecimal transferTo = exchangeDTO.getAmountTo();
		BigDecimal tradeFromSum = tradeFrom.getAmount();
		BigDecimal tradeToSum = tradeTo.getAmount();
		BigDecimal rate = transferFrom.divide(transferTo, RoundingMode.HALF_DOWN);

		CashFlow record = new CashFlow(tradeFrom, tradeTo, transferFrom, transferTo, rate);
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
}
