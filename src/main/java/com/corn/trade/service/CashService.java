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
import java.time.LocalDateTime;
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

	private final TradeLogRepository  tradeLogRepo;
	private final CurrencyRateService currencyRateService;

	public CashService(CashAccountRepository accountRepo,
	                   CashFlowRepository cashFlowRepo,
	                   BrokerRepository brokerRepo,
	                   CurrencyRepository currencyRepo,
	                   CashAccountTypeRepository accountTypeRepo,
	                   TickerRepository tickerRepo,
	                   TradeLogRepository tradeLogRepo, CurrencyRateService currencyRateService) {
		this.accountRepo = accountRepo;
		this.cashFlowRepo = cashFlowRepo;
		this.brokerRepo = brokerRepo;
		this.currencyRepo = currencyRepo;
		this.accountTypeRepo = accountTypeRepo;
		this.tickerRepo = tickerRepo;
		this.tradeLogRepo = tradeLogRepo;
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

		CashAccount trade = transfer(
				transferDTO.getAmount(),
				null,
				broker,
				currency,
				fromIncome,
				toTrade,
				LocalDateTime.now());
		logger.debug("finish");
		return CashAccountMapper.toDTO(trade);
	}

	private CashAccount transfer(double transfer,
	                             TradeLog tradeLog,
	                             Broker broker,
	                             Currency currency,
	                             CashAccountType fromType,
	                             CashAccountType toType,
	                             LocalDateTime dateTime) {
		CashAccount from    = getAccount(broker, currency, fromType);
		CashAccount to      = getAccount(broker, currency, toType);
		Double      fromSum = from.getAmount();
		Double      toSum   = to.getAmount();

		CashFlow record = new CashFlow(from, to, tradeLog, transfer, transfer, null, dateTime);
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

		CashFlow record = new CashFlow(
				tradeFrom,
				tradeTo,
				null,
				transferFrom,
				transferTo,
				rate,
				LocalDateTime.now()
		);
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

	public void fee(Double amount, Broker broker, TradeLog tradeLog, LocalDateTime dateTime) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = currencyRepo.findCurrencyByName("USD");

		transfer(amount, tradeLog, broker, currency, fromTrade, toFee, dateTime);
		logger.debug("finish");
	}

	public void buy(Double amount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toOpen    = accountTypeRepo.findCashAccountTypeByName("open");

		transfer(amount, tradeLog, broker, currency, fromTrade, toOpen, tradeLog.getDateOpen());
		logger.debug("finish");
	}

	public void sell(double openAmount, double closeAmount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType tradeType  = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType openType   = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType   = accountTypeRepo.findCashAccountTypeByName("loss");

		transfer(openAmount, tradeLog, broker, currency, openType, tradeType, tradeLog.getDateClose());

		if (closeAmount > openAmount) {
			double profit = closeAmount - openAmount;
			transfer(profit, tradeLog, broker, currency, profitType, tradeType, tradeLog.getDateClose());
		}

		if (closeAmount < openAmount) {
			double loss = openAmount - closeAmount;
			transfer(loss, tradeLog, broker, currency, tradeType, lossType, tradeLog.getDateClose());
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

		return capital + getAssetsDepositUSD();
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

	public double getAssetsDepositUSD(long brokerId) throws JsonProcessingException {
		Broker               broker = brokerRepo.getReferenceById(brokerId);
		List<CurrencySumDTO> opens  = tradeLogRepo.openSumsByBroker(broker);
		double               sum    = 0.0;
		LocalDate            date   = LocalDate.now();

		for (CurrencySumDTO dto : opens) {
			sum += currencyRateService.convertToUSD(
					dto.getCurrencyId(),
					dto.getSum(),
					date);
		}

		return sum;
	}

	public double getAssetsDepositUSD() throws JsonProcessingException {
		List<CurrencySumDTO> opens = tradeLogRepo.openSums();
		double               sum   = 0.0;
		LocalDate            date  = LocalDate.now();

		for (CurrencySumDTO dto : opens) {
			sum += currencyRateService.convertToUSD(
					dto.getCurrencyId(),
					dto.getSum(),
					date);
		}

		return sum;
	}

	public EvalOutDTO eval(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker    broker     = brokerRepo.getReferenceById(evalDTO.getBrokerId());
		final LocalDate date       = evalDTO.getDate();
		final double    assetsUSD  = getAssetsDepositUSD(broker.getId());
		final double    depositUSD = getDeposit(broker.getId(), date);
		final Ticker    ticker     = tickerRepo.getReferenceById(evalDTO.getTickerId());
		final long      items      = evalDTO.getItems();
		final double    priceOpen  = evalDTO.getPriceOpen();
		final double    fees       = getFees(broker, ticker, items, items * priceOpen).getAmount();
		final long      currencyId = ticker.getCurrency().getId();
		final double    stopLoss   = evalDTO.getStopLoss();
		final int       shortC     = evalDTO.isShort() ? -1 : 1;

		final double feesUSD = currencyRateService.convertToUSD(
				currencyId,
				fees,
				date);

		final double priceUSD =
				currencyRateService.convertToUSD(
						currencyId,
						priceOpen,
						date);

		final double stopLossUSD =
				currencyRateService.convertToUSD(
						currencyId,
						stopLoss,
						date);

		final double sum = items * priceUSD;

		final double sumLoss = items * stopLossUSD;

		final double losses = shortC * sum - (shortC * sumLoss);

		final double feesOpen = getFees(broker, ticker, items, sum).getAmount();

		final double feesLoss = getFees(broker, ticker, items, sumLoss).getAmount();

		final double risk = (losses + feesOpen + feesLoss) / (depositUSD + assetsUSD) * 100.0;

		final double breakEven = getBreakEven(shortC, broker, ticker, items, priceOpen);

		final double takeProfit = getTakeProfit(shortC, broker, ticker, items, priceOpen, stopLoss);

		final double outcomeExp = (shortC * priceOpen - shortC * stopLoss) * 3 * items;

		return new EvalOutDTO(feesUSD, risk, breakEven, takeProfit, outcomeExp);
	}

	private double getBreakEven(int shortC, Broker broker, Ticker ticker, long items, double priceOpen) {
		double sumOpen   = items * priceOpen;
		double feesOpen  = getFees(broker, ticker, items, sumOpen).getAmount();
		double sumClose  = sumOpen;
		double feesClose = getFees(broker, ticker, items, sumClose).getAmount();
		double taxes     = getTaxes(shortC * sumClose - (shortC * sumOpen));

		while (shortC * sumClose - (shortC * sumOpen) < feesOpen + feesClose + taxes) {
			sumClose = sumClose + (shortC * 0.01);
			feesClose = getFees(broker, ticker, items, sumClose).getAmount();
			taxes = getTaxes(shortC * sumClose - (shortC * sumOpen));
		}

		logger.debug("Taxes: {}", taxes);

		return sumClose / items;
	}

	private double getTaxes(double sum) {
		double leftSum = sum;
		double ipn     = leftSum * 0.1; // (ИПН)
		leftSum = leftSum - ipn;

		return sum - leftSum;
	}

	private double getTakeProfit(int shortC, Broker broker, Ticker ticker, long items, double priceOpen, double stopLoss) {
		double sumOpen   = items * priceOpen;
		double feesOpen  = getFees(broker, ticker, items, sumOpen).getAmount();
		double sumClose  = sumOpen;
		double feesClose = getFees(broker, ticker, items, sumClose).getAmount();
		double sumLoss   = items * stopLoss;
		double lossDelta = shortC * sumOpen - shortC * sumLoss;

		while (shortC * sumClose - (shortC * sumOpen) < lossDelta * 3 + feesOpen + feesClose) {
			sumClose = sumClose + (shortC * 0.01);
			feesClose = getFees(broker, ticker, items, sumClose).getAmount();
		}

		return sumClose / items;
	}

	public Fees getFees(Broker broker, Ticker ticker, long items, Double sum) {
		double fixed  = 0.0;
		double fly    = 0.0;
		double amount = 0.0;

		if (broker.getName().equals("FreedomFN")) {
			if (ticker.getCurrency().getName().equals("KZT")) {
				amount = fly = sum / 100.0 * 0.085;
			} else {
				fixed = 1.2;
				fly = sum / 100.0 * 0.5 + items * 0.012;
				amount = fixed + fly;
			}
		}
		return new Fees(fixed, fly, amount);
	}
}
