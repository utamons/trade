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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@Service
@Transactional
public class CashService {

	private final static double MAX_RISK_PC = 2.0;
	private final static double MAX_BE_PC   = 1.65;

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

	public CashAccount getAccount(Broker broker, Currency currency, CashAccountType type) {
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

	public CashAccount transfer(double transfer,
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

	public void sellShort(Double amount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType fromBorrowed = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType toOpen       = accountTypeRepo.findCashAccountTypeByName("open");

		transfer(amount, tradeLog, broker, currency, fromBorrowed, toOpen, tradeLog.getDateOpen());
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

	public void buyShort(double openAmount, double closeAmount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		CashAccountType tradeType  = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType borrowedType  = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType openType   = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType   = accountTypeRepo.findCashAccountTypeByName("loss");

		transfer(openAmount, tradeLog, broker, currency, openType, borrowedType, tradeLog.getDateClose());

		if (closeAmount < openAmount) {
			double profit = openAmount - closeAmount;
			transfer(profit, tradeLog, broker, currency, profitType, tradeType, tradeLog.getDateClose());
		}

		if (closeAmount > openAmount) {
			double loss = closeAmount - openAmount;
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

	public List<CashAccountDTO> getBorrowedAccounts(Long brokerId) {
		Broker          broker    = brokerRepo.getReferenceById(brokerId);
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("borrowed");
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

	public double getRiskBase(double capital) {
		return capital + (12 - LocalDate.now().getMonthValue()) * 1000;
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
		List<CurrencySumDTO> opens = tradeLogRepo.openLongSums();
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

	public EvalOutFitDTO evalToFit(EvalInDTO evalDTO) throws JsonProcessingException {
		logger.debug("start");
		final int    shortC     = evalDTO.isShort() ? -1 : 1;
		EvalOutDTO   dto;
		double       be, risk   = 0, bePc = 0;
		final Ticker ticker     = tickerRepo.getReferenceById(evalDTO.getTickerId());
		final long   currencyId = ticker.getCurrency().getId();
		double       depositUS  = getDeposit(evalDTO.getBrokerId(), LocalDate.now());

		double volume;
		if (evalDTO.getItems() != null && evalDTO.getStopLoss() != null) {
			dto = eval(evalDTO);
			be = dto.getBreakEven().doubleValue();
			risk = dto.getRisk().doubleValue();
			bePc = Math.abs(be / evalDTO.getPriceOpen() * 100.0 - 100.0);
			if (risk <= MAX_RISK_PC && bePc <= MAX_BE_PC)
				return new EvalOutFitDTO(dto.getFees(), dto.getRisk(), dto.getBreakEven(), dto.getTakeProfit(),
				                         dto.getOutcomeExp(), evalDTO.getStopLoss(), evalDTO.getItems());
		}

		double stopLoss = evalDTO.getPriceOpen();
		int    count    = 0;
		if (evalDTO.getItems() == null) {
			long   items = 0;
			double bePcPrev;
			do {
				++items;
				volume = currencyRateService.convertToUSD(currencyId, evalDTO.getPriceOpen() * items, LocalDate.now());
				evalDTO.setStopLoss(stopLoss);
				evalDTO.setItems(items);
				if (depositUS - volume < 0) {
					logger.debug("Too much items for current deposit");
					break;
				}
				dto = eval(evalDTO);
				be = dto.getBreakEven().doubleValue();
				bePcPrev = bePc;
				bePc = (shortC > 0 ? be / evalDTO.getPriceOpen() : evalDTO.getPriceOpen() / be) * 100.0 - 100.0;
				if (be == bePcPrev && count < 3) {
					count++;
				} else {
					count = 0;
				}
				logger.debug("BE PC: {}, Prev: {}, Items: {}", bePc, bePcPrev, items);
			} while (bePc > MAX_BE_PC);
		}

		double riskPrev;
		double step = 0.01;
		double stopLossPc;
		do {
			stopLoss -= (shortC * step);
			stopLossPc = shortC > 0 ? stopLoss / evalDTO.getPriceOpen() : evalDTO.getPriceOpen() / stopLoss;
			if (stopLoss < 1.0 || stopLossPc <= 0.8)
				break;
			evalDTO.setStopLoss(stopLoss);
			riskPrev = risk;
			risk = Math.round(getRisk(evalDTO) * 100) / 100.0;
			if (risk == riskPrev)
				step++;
			logger.debug("risk: {}", risk);

		} while (risk < MAX_RISK_PC);

		if (risk > MAX_RISK_PC && stopLossPc >= 0.8) {
			stopLoss += (shortC * step);
			evalDTO.setStopLoss(stopLoss);
		}

		dto = eval(evalDTO);

		logger.debug("finish");
		if (dto != null)
			return new EvalOutFitDTO(dto.getFees(), dto.getRisk(), dto.getBreakEven(), dto.getTakeProfit(),
			                         dto.getOutcomeExp(), evalDTO.getStopLoss(), evalDTO.getItems());
		else
			throw new RuntimeException("Cannot evaluate to fit!");
	}

	public EvalOutDTO eval(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker    broker     = brokerRepo.getReferenceById(evalDTO.getBrokerId());
		final LocalDate date       = evalDTO.getDate();
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

		final double risk = getRisk(evalDTO);

		final double breakEven = getBreakEven(shortC, broker, ticker, items, priceOpen, 0);

		final double takeProfit = getTakeProfit(shortC, broker, ticker, items, priceOpen, stopLoss);

		final double outcomeExp = (shortC * priceOpen - shortC * stopLoss) * 3 * items;

		return new EvalOutDTO(feesUSD, risk, breakEven, takeProfit, outcomeExp);
	}

	public double getRisk(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker    broker     = brokerRepo.getReferenceById(evalDTO.getBrokerId());
		final LocalDate date       = evalDTO.getDate();
		final double    capital    = getCapital();
		final double    priceOpen  = evalDTO.getPriceOpen();
		final Ticker    ticker     = tickerRepo.getReferenceById(evalDTO.getTickerId());
		final long      currencyId = ticker.getCurrency().getId();
		final double    stopLoss   = evalDTO.getStopLoss();
		final int       shortC     = evalDTO.isShort() ? -1 : 1;

		final double riskBase = getRiskBase(capital);

		final long items = evalDTO.getItems();

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

		return (losses + feesOpen + feesLoss) / (riskBase) * 100.0;
	}

	private double getBreakEven(int shortC, Broker broker, Ticker ticker, long items, double priceOpen, double interest) {
		double sumOpen   = items * priceOpen;
		double feesOpen  = getFees(broker, ticker, items, sumOpen).getAmount();
		double sumClose  = sumOpen;
		double feesClose = getFees(broker, ticker, items, sumClose).getAmount();
		double taxes     = getTaxes(shortC * sumClose - (shortC * sumOpen));

		while (shortC * sumClose - (shortC * sumOpen) < feesOpen + feesClose + taxes + interest) {
			sumClose = sumClose + (shortC * 0.01);
			feesClose = getFees(broker, ticker, items, sumClose).getAmount();
			taxes = getTaxes(shortC * sumClose - (shortC * sumOpen));
		}

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

	public void correction(TransferDTO transferDTO) {
		Currency currency = currencyRepo.getReferenceById(transferDTO.getCurrencyId());
		Broker   broker   = brokerRepo.getReferenceById(transferDTO.getBrokerId());
		double   amount   = transferDTO.getAmount();

		CashAccountType from, to;
		if (amount > 0) {
			from = accountTypeRepo.findCashAccountTypeByName("correction");
			to = accountTypeRepo.findCashAccountTypeByName("trade");
		} else {
			from = accountTypeRepo.findCashAccountTypeByName("trade");
			to = accountTypeRepo.findCashAccountTypeByName("correction");
		}
		transfer(Math.abs(amount), null, broker, currency, from, to, LocalDateTime.now());
	}


	public void applyBorrowInterest(TradeLog tradeLog) throws JsonProcessingException {
		if (tradeLog.isClosed() || tradeLog.isLong()) {
			return;
		}
		LocalDate openDate = tradeLog.getDateOpen().toLocalDate();
		LocalDate currentDate = LocalDate.now();
		long daysBetween = ChronoUnit.DAYS.between(openDate, currentDate);
		if (tradeLog.getBroker().getName().equals("FreedomFN")) {
			double interest = (tradeLog.getVolume()/100.0*12.0/365.0)*daysBetween;
			double interestUSD = currencyRateService.convertToUSD(tradeLog.getTicker().getCurrency().getId(), interest, currentDate);
			tradeLog.setBrokerInterest(interestUSD);
			double breakEven = getBreakEven(-1,
			                                tradeLog.getBroker(),
			                                tradeLog.getTicker(),
			                                tradeLog.getItemNumber(),
			                                tradeLog.getPriceOpen(),
			                                interest);
			tradeLog.setBreakEven(breakEven);
			tradeLogRepo.save(tradeLog);
		}
	}
}


