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
	private final static Logger logger          = LoggerFactory.getLogger(CashService.class);
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
		CashAccountType tradeType    = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType borrowedType = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType openType     = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType   = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType     = accountTypeRepo.findCashAccountTypeByName("loss");

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
		return capital;
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

	public EvalOutFitDTO evalToFit(EvalInFitDTO evalDTO) throws JsonProcessingException {
		logger.debug("start");
		final int    shortC     = evalDTO.isShort() ? -1 : 1;
		Double       levelPrice = evalDTO.levelPrice();
		Double       atr        = evalDTO.atr();
		Currency    currency   = tickerRepo.getReferenceById(evalDTO.tickerId()).getCurrency();
		double price = levelPrice + (shortC * 0.05);
		double stopLoss = evalDTO.stopLoss() == null ? levelPrice - (shortC * levelPrice/100*0.2) : evalDTO.stopLoss();
		logger.debug("stopLoss: {}", stopLoss);
		double takeProfit = price + (shortC * atr * 0.7);
		long items = 0L;
		double volumePc = 0, prevVolumePc;
		EvalOutDTO eval = null, prev;
		do {
			items++;
			prevVolumePc = volumePc;
			double volume = price * items;
			double volumeUSD = currencyRateService.convertToUSD(currency.getId(), volume, LocalDate.now());
			double capital = getCapital();
			volumePc = volumeUSD / capital * 100.0;
			EvalInDTO dto = new EvalInDTO(
					evalDTO.brokerId(),
					evalDTO.tickerId(),
					price,
					atr,
					items,
					stopLoss,
					takeProfit,
					LocalDate.now(),
					evalDTO.isShort());
			prev = eval;

			eval = eval(dto);

			logger.debug("items: {}", items);
			logger.debug("volumePc: {}, dto: {}", volumePc, evalDTO.depositPc());
			logger.debug("riskPc: {}, dto: {}", eval.riskPc(), evalDTO.riskPc());
			logger.debug("riskRewardPc: {}, dto: {}", eval.riskRewardPc(), evalDTO.riskRewardPc());

		} while (eval.riskPc() <= evalDTO.riskPc() && volumePc <= evalDTO.depositPc());

		if (prev == null) {
			return null;
		}

		return new EvalOutFitDTO(
				prev.fees(),
				prev.riskPc(),
				prev.breakEven(),
				takeProfit,
				prev.outcomeExp(),
				stopLoss,
				price,
				items,
				prev.volume(),
				prev.gainPc(),
				prev.riskRewardPc(),
				prevVolumePc
		);
	}

	public EvalOutDTO eval(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker    broker     = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker    ticker     = tickerRepo.getReferenceById(evalDTO.tickerId());
		final long      items      = evalDTO.items();
		final double    priceOpen  = evalDTO.price();
		final double    volume     = priceOpen * items;
		final double    feesOpen   = getFees(broker, ticker, items, volume).getAmount();
		final double    takeProfit = evalDTO.takeProfit();
		final double    stopLoss   = evalDTO.stopLoss();
		final int       shortC     = evalDTO.isShort() ? -1 : 1;

		final double breakEven = getBreakEven(shortC, broker, ticker, items, priceOpen, 0);

		final double profit = Math.abs(takeProfit - breakEven) * items;

		final double riskRewardPc = Math.abs(stopLoss - breakEven) / Math.abs(takeProfit - breakEven) * 100;

		final double riskPc = getRisk(evalDTO);

		double feesClose = getFees(broker, ticker, items, takeProfit * items).getAmount();
		double taxes     = getTaxes(profit);

		final double outcomeExp = Math.abs(profit - feesOpen - feesClose - taxes);

		final double gainPc = outcomeExp / volume * 100;

		return new EvalOutDTO(outcomeExp, gainPc, (feesOpen + feesClose), riskPc, riskRewardPc, breakEven, volume);
	}

	public double getRisk(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker    broker     = brokerRepo.getReferenceById(evalDTO.brokerId());
		final LocalDate date       = evalDTO.date();
		final double    capital    = getCapital();
		final double    priceOpen  = evalDTO.price();
		final Ticker    ticker     = tickerRepo.getReferenceById(evalDTO.tickerId());
		final long      currencyId = ticker.getCurrency().getId();
		final double    stopLoss   = evalDTO.stopLoss();

		final double riskBase = getRiskBase(capital);

		final long items = evalDTO.items();

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

		final double losses = Math.abs(sum - sumLoss);

		final double feesOpen = getFees(broker, ticker, items, sum).getAmount();

		final double feesLoss = getFees(broker, ticker, items, sumLoss).getAmount();

		return (losses + feesOpen + feesLoss) / (riskBase) * 100.0;
	}

	private double getBreakEven(int shortC,
	                            Broker broker,
	                            Ticker ticker,
	                            long items,
	                            double priceOpen,
	                            double interest) throws JsonProcessingException {
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



	public Fees getFees(Broker broker, Ticker ticker, long items, Double sum) throws JsonProcessingException {
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
		} else if (broker.getName().equals("Interactive")) {
			if (ticker.getCurrency().getName().equals("USD")) {
				double max = items * sum / 100.0;
				double min = items * 0.005;
				amount = Math.min(max, min);
				amount = amount < 1 ? 1 : amount;
			}
		}
		amount = currencyRateService.convertToUSD(ticker.getCurrency().getId(), amount, LocalDate.now());
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
		LocalDate openDate    = tradeLog.getDateOpen().toLocalDate();
		LocalDate currentDate = LocalDate.now();
		long      daysBetween = ChronoUnit.DAYS.between(openDate, currentDate);
		if (tradeLog.getBroker().getName().equals("FreedomFN")) {
			double interest = (tradeLog.getVolume() / 100.0 * 12.0 / 365.0) * daysBetween;
			double interestUSD =
					currencyRateService.convertToUSD(tradeLog.getTicker().getCurrency().getId(), interest, currentDate);
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


