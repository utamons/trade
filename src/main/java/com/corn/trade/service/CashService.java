package com.corn.trade.service;

import com.corn.trade.dto.*;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.CashAccountMapper;
import com.corn.trade.mapper.CurrencyMapper;
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
	private final static Logger                    logger = LoggerFactory.getLogger(CashService.class);
	private final        CashAccountRepository     accountRepo;
	private final        CashFlowRepository        cashFlowRepo;
	private final        BrokerRepository          brokerRepo;
	private final        CurrencyRepository        currencyRepo;
	private final        CashAccountTypeRepository accountTypeRepo;
	private final        TickerRepository          tickerRepo;

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

	/**
	 * Get cash account by broker, currency and type. If account not found, create new one.
	 *
	 * @param broker broker
	 * @param currency currency
	 * @param type account type
	 * @return cash account
	 */
	public CashAccount getAccount(Broker broker, Currency currency, CashAccountType type) {
		logger.debug("start");
		CashAccount account = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker, currency, type);
		if (account == null) {
			logger.debug("Account '{}' not found for broker {} and currency {}. Creating...",
			             type.getName(), broker.getName(), currency.getName());
			String accountName = type.getName() + "/" + broker.getName() + "/" + currency.getName().trim(); // H2 adds trailing spaces to string
			account = accountRepo.save(new CashAccount(accountName, currency, broker, type));
		} else {
			logger.debug("Found {} account for broker {} and currency {}.",
			             type.getName(), broker.getName(), currency.getName());
		}
		logger.debug("finish");
		return account;
	}

	/**
	 * Refills trade account with money.
	 * <p>
	 * Creates a cash flow record for the refill.
	 * <p>
	 * see {@link #transfer(double, TradeLog, Broker, Currency, CashAccountType, CashAccountType, LocalDateTime)}
	 * @param transferDTO transfer data
	 * @return trade account
	 */
	public CashAccountDTO refill(TransferDTO transferDTO) {
		logger.debug("start");
		Broker          broker     = brokerRepo.getReferenceById(transferDTO.brokerId());
		Currency        currency   = currencyRepo.getReferenceById(transferDTO.currencyId());
		CashAccountType toTrade    = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType fromIncome = accountTypeRepo.findCashAccountTypeByName("income");

		CashAccount trade = transfer(
				transferDTO.amount(),
				null,
				broker,
				currency,
				fromIncome,
				toTrade,
				LocalDateTime.now());
		logger.debug("finish");
		return CashAccountMapper.toDTO(trade);
	}

	/**
	 * Transfer money from one account to another.
	 * Both accounts must be of the same broker and currency.
	 * <p>
	 * Creates a cash flow record for the transfer.
	 *
	 * @param transfer amount to transfer
	 * @param tradeLog trade log (optional)
	 * @param broker broker
	 * @param currency currency
	 * @param fromType account type to transfer from
	 * @param toType account type to transfer to
	 * @param dateTime date and time of transfer
	 * @return account to transfer to
	 */
	public CashAccount transfer(double transfer,
	                            TradeLog tradeLog,
	                            Broker broker,
	                            Currency currency,
	                            CashAccountType fromType,
	                            CashAccountType toType,
	                            LocalDateTime dateTime) {
		CashAccount from    = getAccount(broker, currency, fromType);
		CashAccount to      = getAccount(broker, currency, toType);

		CashFlow record = new CashFlow(from, to, tradeLog, transfer, transfer, null, dateTime);
		cashFlowRepo.save(record);

		to = accountRepo.save(to);
		accountRepo.save(from);

		cashFlowRepo.flush();
		accountRepo.flush();

		logger.debug("Transfer for amount {}, broker {} and currency {} is finished",
		             transfer, broker.getName(), currency.getName());
		return to;
	}

	/**
	 * Transfers money from one trade account to another with a different currency.
	 * A currency conversion rate is calculated based on transfer sums.
	 * <p>
	 * Creates a cash flow record for the transfer.
	 *
	 * @param exchangeDTO exchange data
	 * @return trade account
	 */
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

	/**
	 * Withdraws broker fee from trade account.
	 * <p>
	 * <b>The trade account must be of the same currency as the broker fee.</b>
	 * <p>
	 * Creates a cash flow record for the transfer.
	 * @param amount amount to withdraw
	 * @param broker broker
	 * @param tradeLog trade record related to the fee (optional)
	 * @param dateTime date and time of withdrawal
	 */
	public void fee(Double amount, Broker broker, TradeLog tradeLog, LocalDateTime dateTime) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = broker.getFeeCurrency();

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

	/**
	 * Selling a short position (opening a short position).
	 * <p>
	 * Transfers money from trade account to borrowed account.
	 * <p>
	 * Creates a cash flow record for the transfer.
	 * @param amount amount to transfer
	 * @param broker broker
	 * @param currency currency
	 * @param tradeLog trade record related to the transfer (required)
	 */
	public void sellShort(Double amount, double openFees, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		// todo re-check and re-test
		// todo fix docs
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType fromBorrowed = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType toOpen       = accountTypeRepo.findCashAccountTypeByName("open");
		if (fromBorrowed == null || toOpen == null) {
			throw new IllegalStateException("Account types are not found");
		}

		transfer(amount, tradeLog, broker, currency, fromBorrowed, toOpen, tradeLog.getDateOpen());
		fee(openFees, broker, tradeLog, tradeLog.getDateClose());
		logger.debug("finish");
	}

	/**
	 * Selling a long position (closing a long position).
	 * <p>
	 * Transfers open amount (total bought) back from open account to trade account.
	 * Any profit is transferred from profit account to trade account.
	 * Any loss is transferred from trade account to loss account.
	 * <p>
	 * Creates a cash flow record for each transfer.
	 * @param closeAmount amount to close (total sold)
	 * @param closeFees broker's fees for closing the position (commission, exchange fees, etc.)
	 * @param broker broker
	 * @param tradeLog trade record related to the transfer (required)
	 */
	public void sell(double closeAmount, double closeFees, Broker broker, TradeLog tradeLog) throws JsonProcessingException {
		logger.debug("start");
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType tradeType  = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType openType   = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType profitType = accountTypeRepo.findCashAccountTypeByName("profit");
		CashAccountType lossType   = accountTypeRepo.findCashAccountTypeByName("loss");

		if (tradeType == null || openType == null || profitType == null || lossType == null) {
			throw new IllegalStateException("Account types are not found");
		}

		double openAmount = tradeLog.getTotalBought();
		Currency tradeCurrency = tradeLog.getCurrency();
		Currency feeCurrency   = broker.getFeeCurrency();

		double openFeesConverted   = currencyRateService.convert(feeCurrency, tradeCurrency, tradeLog.getOpenCommission(), tradeLog.getDateClose().toLocalDate());
		double closeFeesConverted = currencyRateService.convert(feeCurrency, tradeCurrency, closeFees, tradeLog.getDateClose().toLocalDate());

		transfer(openAmount, tradeLog, broker, tradeCurrency, openType, tradeType, tradeLog.getDateClose());
		fee(closeFees, broker, tradeLog, tradeLog.getDateClose());

		if (closeAmount - closeFeesConverted - openFeesConverted > openAmount) {
			// todo Re-think well again if we should use fees here
			double profit = closeAmount - openAmount - closeFeesConverted - openFeesConverted;
			transfer(profit, tradeLog, broker, tradeCurrency, profitType, tradeType, tradeLog.getDateClose());
		}

		if (closeAmount - closeFeesConverted - openFeesConverted < openAmount) {
			double loss = openAmount - closeAmount + closeFeesConverted + openFeesConverted;
			transfer(loss, tradeLog, broker, tradeCurrency, tradeType, lossType, tradeLog.getDateClose());
		}

		logger.debug("finish");
	}

	public void buyShort(double openAmount, double closeAmount, Broker broker, Currency currency, TradeLog tradeLog) {
		logger.debug("start");
		// todo include all fees operations with broker interest
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
		return getAccountTotal(trade);
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
			double amount = getAccountTotal(account);
			capital = capital + currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					amount,
					today);
		}

		return capital + getAssetsDepositUSD();
	}

	public double getAccountTotal(CashAccount account) {
		Double sumFrom = cashFlowRepo.getSumFromByAccount(account);
		Double sumTo   = cashFlowRepo.getSumToByAccount(account);
		if (sumFrom == null) {
			sumFrom = 0.0;
		}
		if (sumTo == null) {
			sumTo = 0.0;
		}
		return sumTo - sumFrom;
	}

	public double getAccountTotal(CashAccountDTO accountDTO) {
		CashAccount account = accountRepo.getReferenceById(accountDTO.id());
		return getAccountTotal(account);
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

	public record EvalToFitRecord(long items, double volumePc, EvalOutDTO eval) {
	}

	public long getMaxItems(Currency currency, double depositPc, double price) throws JsonProcessingException {
		double     capital      = getCapital();
		double     maxVolumeUSD = capital * depositPc / 100.0;
		double     priceUSD     = currencyRateService.convertToUSD(currency.getId(), price, LocalDate.now());
		return (long) (maxVolumeUSD / priceUSD);
	}

	public EvalToFitRecord evalToFit(EvalInFitDTO evalDTO,
	                                 Currency currency,
	                                 Double atr,
	                                 Double stopLoss,
	                                 Double takeProfit,
	                                 double price,
									 double capital) throws JsonProcessingException {
		double     volumePc;
		EvalOutDTO eval;
		long items = getMaxItems(currency, evalDTO.depositPc(), price);
		final Broker broker     = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker ticker     = tickerRepo.getReferenceById(evalDTO.tickerId());
		final String brokerName = broker.getName();
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(ticker.getCurrency());
		EvalInDTO dto;
		do {
			double volume    = price * items;
			double volumeUSD = currencyRateService.convertToUSD(currency.getId(), volume, LocalDate.now());
			volumePc = volumeUSD / capital * 100.0;
			dto = new EvalInDTO(
					evalDTO.brokerId(),
					evalDTO.tickerId(),
					price,
					atr,
					items,
					stopLoss,
					takeProfit,
					LocalDate.now(),
					evalDTO.isShort());
			eval = eval(dto, brokerName, currencyDTO, capital);
			items--;
		} while (eval.riskPc() > evalDTO.riskPc() && eval.riskRewardPc() < evalDTO.riskRewardPc());
		return new EvalToFitRecord(items, volumePc, eval);
	}


	public EvalOutFitDTO evalToFit(EvalInFitDTO evalDTO) throws JsonProcessingException {
		final int shortC     = evalDTO.isShort() ? -1 : 1;
		Double    levelPrice = evalDTO.levelPrice();
		Double    atr        = evalDTO.atr();
		Currency  currency   = tickerRepo.getReferenceById(evalDTO.tickerId()).getCurrency();
		double    price      = levelPrice + (shortC * 0.05);
		double capital = getCapital();
		double stopLoss =
				evalDTO.stopLoss() == null ? levelPrice - (shortC * levelPrice / 100 * 0.2) : evalDTO.stopLoss();
		double takeProfit = price + (shortC * atr * 0.7);

		EvalToFitRecord evalToFitRecord;

		if (evalDTO.technicalStop()) {
			stopLoss = getTechnicalStop(evalDTO, shortC, atr, currency, price, stopLoss, takeProfit);
			stopLoss = stopLoss + (shortC * 0.01);
		}
		evalToFitRecord = evalToFit(evalDTO, currency, atr, stopLoss, takeProfit, price, capital);

		if (evalToFitRecord == null) {
			return null;
		}

		return new EvalOutFitDTO(
				evalToFitRecord.eval.fees(),
				evalToFitRecord.eval.riskPc(),
				evalToFitRecord.eval.breakEven(),
				takeProfit,
				evalToFitRecord.eval.outcomeExp(),
				stopLoss,
				price,
				evalToFitRecord.items + 1,
				evalToFitRecord.eval.volume(),
				evalToFitRecord.eval.gainPc(),
				evalToFitRecord.eval.riskRewardPc(),
				evalToFitRecord.volumePc
		);
	}

	private double getTechnicalStop(EvalInFitDTO evalDTO,
	                                int shortC,
	                                Double atr,
	                                Currency currency,
	                                double price,
	                                double stopLoss,
	                                double takeProfit) throws JsonProcessingException {
		EvalToFitRecord evalToFitRecord;
		long            ms   = System.currentTimeMillis();
		double          rrPC = 0;
		double capital = getCapital();
		while (rrPC <= evalDTO.riskRewardPc()) {
			stopLoss = stopLoss - (shortC * 0.01);
			evalToFitRecord = evalToFit(evalDTO, currency, atr, stopLoss, takeProfit, price, capital);
			rrPC = evalToFitRecord.eval.riskRewardPc();
		}
		long time = System.currentTimeMillis() - ms;
		logger.debug("time {}", time);
		return stopLoss;
	}

	public EvalOutDTO eval(EvalInDTO evalDTO) throws JsonProcessingException {
		final Broker broker     = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker ticker     = tickerRepo.getReferenceById(evalDTO.tickerId());
		final String brokerName = broker.getName();
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(ticker.getCurrency());
		final double capital = getCapital();
		return eval(evalDTO, brokerName, currencyDTO, capital);
	}


	public EvalOutDTO eval(EvalInDTO evalDTO, String brokerName, CurrencyDTO currencyDTO, double capital) throws JsonProcessingException {
		final long   items      = evalDTO.items();
		final double priceOpen  = evalDTO.price();
		final double volume     = priceOpen * items;
		final double feesOpen   = getFees(brokerName, currencyDTO, items, volume).getAmount();
		final double takeProfit = evalDTO.takeProfit();
		final double stopLoss   = evalDTO.stopLoss();
		final int    shortC     = evalDTO.isShort() ? -1 : 1;

		final double breakEven = getBreakEven(shortC, brokerName, currencyDTO, items, priceOpen, 0);

		final double profit = Math.abs(takeProfit - breakEven) * items;

		final double riskRewardPc = Math.abs(stopLoss - breakEven) / Math.abs(takeProfit - breakEven) * 100;

		final double riskPc = getRisk(evalDTO, brokerName, currencyDTO, capital);

		double feesClose = getFees(brokerName, currencyDTO, items, takeProfit * items).getAmount();
		double taxes     = getTaxes(profit);

		final double outcomeExp = Math.abs(profit - feesOpen - feesClose - taxes);

		final double gainPc = outcomeExp / volume * 100;

		return new EvalOutDTO(outcomeExp, gainPc, feesOpen, riskPc, riskRewardPc, breakEven, volume);
	}

	public double getRisk(EvalInDTO evalDTO, String brokerName, CurrencyDTO currencyDTO, double    capital) throws JsonProcessingException {
		final LocalDate date       = evalDTO.date();
		final double    priceOpen  = evalDTO.price();
		final long      currencyId = currencyDTO.getId();
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

		final double feesOpen = getFees(brokerName, currencyDTO, items, sum).getAmount();

		final double feesLoss = getFees(brokerName, currencyDTO, items, sumLoss).getAmount();

		return (losses + feesOpen + feesLoss) / (riskBase) * 100.0;
	}

	private double getBreakEven(int shortC,
	                            String brokerName,
	                            CurrencyDTO currencyDTO,
	                            long items,
	                            double priceOpen,
	                            double interest) throws JsonProcessingException {
		double sumOpen   = items * priceOpen;
		double feesOpen  = getFees(brokerName, currencyDTO, items, sumOpen).getAmount();
		double sumClose  = sumOpen;
		double feesClose = getFees(brokerName, currencyDTO, items, sumClose).getAmount();
		double taxes     = getTaxes(shortC * sumClose - (shortC * sumOpen));

		while (shortC * sumClose - (shortC * sumOpen) < feesOpen + feesClose + taxes + interest) {
			sumClose = sumClose + (shortC * 0.01);
			feesClose = getFees(brokerName, currencyDTO, items, sumClose).getAmount();
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


	public Fees getFees(String brokerName, CurrencyDTO currencyDTO, long items, Double sum) throws JsonProcessingException {
		double fixed  = 0.0;
		double fly    = 0.0;
		double amount = 0.0;

		if (brokerName.equals("FreedomFN")) {
			if (currencyDTO.getName().equals("KZT")) {
				amount = fly = sum / 100.0 * 0.085;
			} else {
				fixed = 1.2;
				fly = sum / 100.0 * 0.5 + items * 0.012;
				amount = fixed + fly;
			}
		} else if (brokerName.equals("Interactive")) {
			if (currencyDTO.getName().equals("USD")) {
				double max = items * sum / 100.0;
				double min = items * 0.005;
				amount = Math.min(max, min);
				amount = amount < 1 ? 1 : amount;
			}
		}
		amount = currencyRateService.convertToUSD(currencyDTO.getId(), amount, LocalDate.now());
		return new Fees(fixed, fly, amount);
	}

	/**
	 * Performs a correction of the trade account.
	 * <p>
	 * Creates a cash flow record for the operation.
	 *
	 * @param transferDTO transfer data
	 */
	public void correction(TransferDTO transferDTO) {
		Currency currency = currencyRepo.getReferenceById(transferDTO.currencyId());
		Broker   broker   = brokerRepo.getReferenceById(transferDTO.brokerId());
		double   amount   = transferDTO.amount();

		CashAccountType correctionType = accountTypeRepo.findCashAccountTypeByName("correction");
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName("trade");

		CashAccountType from = amount > 0 ? correctionType : tradeType;
		CashAccountType to = amount > 0 ? tradeType : correctionType;

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
			double interest = (tradeLog.getTotalSold() / 100.0 * 12.0 / 365.0) * daysBetween;
			double interestUSD =
					currencyRateService.convertToUSD(tradeLog.getTicker().getCurrency().getId(), interest, currentDate);
			tradeLog.setBrokerInterest(interestUSD);
			double breakEven = getBreakEven(-1,
			                                tradeLog.getBroker().getName(),
			                                CurrencyMapper.toDTO(tradeLog.getTicker().getCurrency()),
			                                tradeLog.getItemSold(),
			                                tradeLog.getEstimatedPriceOpen(),
			                                interest);
			tradeLog.setEstimatedBreakEven(breakEven);
			tradeLogRepo.save(tradeLog);
		}
	}
}


