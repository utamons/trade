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
import java.util.List;
import java.util.stream.Collectors;

import static com.corn.trade.util.Util.round;
import static java.lang.Math.abs;

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
	 * @param broker   broker
	 * @param currency currency
	 * @param type     account type
	 * @return cash account
	 */
	public CashAccount getAccount(Broker broker, Currency currency, CashAccountType type) {
		logger.debug("start");
		CashAccount account = accountRepo.findCashAccountByBrokerAndCurrencyAndType(broker, currency, type);
		if (account == null) {
			logger.debug("Account '{}' not found for broker {} and currency {}. Creating...",
			             type.getName(), broker.getName(), currency.getName());
			String accountName = type.getName() +
			                     "/" +
			                     broker.getName() +
			                     "/" +
			                     currency.getName().trim(); // H2 adds trailing spaces to string
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
	 *
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
	 * @param broker   broker
	 * @param currency currency
	 * @param fromType account type to transfer from
	 * @param toType   account type to transfer to
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
		if (broker == null) {
			throw new IllegalArgumentException("Broker is null");
		}
		if (currency == null) {
			throw new IllegalArgumentException("Currency is null");
		}
		if (fromType == null) {
			throw new IllegalArgumentException("From type is null");
		}
		if (toType == null) {
			throw new IllegalArgumentException("To type is null");
		}
		if (dateTime == null) {
			throw new IllegalArgumentException("Date and time is null");
		}
		if (transfer == 0) {
			logger.debug("Transfer amount is zero. Nothing to do.");
			return null;
		}


		CashAccount from = getAccount(broker, currency, fromType);
		CashAccount to   = getAccount(broker, currency, toType);

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
	 */
	public void exchange(ExchangeDTO exchangeDTO) {
		logger.debug("start");
		Broker          broker         = brokerRepo.getReferenceById(exchangeDTO.getBrokerId());
		Currency        currencyFrom   = currencyRepo.getReferenceById(exchangeDTO.getCurrencyFromId());
		Currency        currencyTo     = currencyRepo.getReferenceById(exchangeDTO.getCurrencyToId());
		CashAccountType tradeType      = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType conversionType = accountTypeRepo.findCashAccountTypeByName("conversion");

		CashAccount tradeFrom    = getAccount(broker, currencyFrom, tradeType);
		CashAccount tradeTo      = getAccount(broker, currencyTo, tradeType);
		CashAccount conversionTo = getAccount(broker, currencyTo, conversionType);
		double      transferFrom = exchangeDTO.getAmountFrom();
		double      transferTo   = exchangeDTO.getAmountTo();
		double      delta        = transferFrom - transferTo;

		CashFlow record = new CashFlow(
				tradeFrom,
				tradeTo,
				null,
				transferFrom,
				transferFrom,
				null,
				LocalDateTime.now()
		);
		cashFlowRepo.save(record);

		CashFlow conversion = new CashFlow(
				tradeTo,
				conversionTo,
				null,
				delta,
				delta,
				null,
				LocalDateTime.now()
		);
		cashFlowRepo.save(conversion);

		cashFlowRepo.flush();

		logger.debug("Exchange for amount from {}, to {}, broker {} and currency from {}, to {} is finished",
		             transferFrom, transferTo, broker.getName(), currencyFrom.getName(),
		             currencyTo.getName());

		logger.debug("finish");
	}

	/**
	 * Withdraws broker fee from trade account.
	 * <p>
	 * <b>The trade account must be of the same currency as the broker fee.</b>
	 * <p>
	 * Creates a cash flow record for the transfer.
	 *
	 * @param amount   amount to withdraw
	 * @param broker   broker
	 * @param tradeLog trade record related to the fee (optional)
	 * @param dateTime date and time of withdrawal
	 */
	public void fee(double amount, Broker broker, TradeLog tradeLog, LocalDateTime dateTime) {
		logger.debug("start");
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = broker.getFeeCurrency();
		if (amount == 0) {
			logger.debug("Fee amount is zero. Nothing to do.");
			return;
		}
		if (currency == null) {
			throw new IllegalArgumentException("Broker fee currency is null");
		}
		if (fromTrade == null) {
			throw new IllegalArgumentException("trade type is null");
		}
		if (toFee == null) {
			throw new IllegalArgumentException("fee type is null");
		}

		transfer(amount, tradeLog, broker, currency, fromTrade, toFee, dateTime);
		logger.debug("finish");
	}

	/**
	 * Selling a short position (opening a short position).
	 * <p>
	 * Transfers money from borrowed account to open account.
	 * Transfers fee from trade account to fee account.
	 * Updates trade log record with the trade data.
	 * <p>
	 * Creates cash flow records for transfers.
	 *
	 * @param itemSold       item sold
	 * @param amountSold     amount to transfer
	 * @param openCommission commission for opening a position
	 * @param dateOpen       date and time of opening a position
	 * @param broker         broker
	 * @param currency       currency
	 * @param tradeLog       trade record related to the transfer (required)
	 */
	public void sellShort(long itemSold,
	                      double amountSold,
	                      double openCommission,
	                      LocalDateTime dateOpen,
	                      Broker broker,
	                      Currency currency,
	                      TradeLog tradeLog) {
		logger.debug("start");
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType fromBorrowed = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType toOpen       = accountTypeRepo.findCashAccountTypeByName("open");
		if (fromBorrowed == null || toOpen == null) {
			throw new IllegalStateException("Account types are not found");
		}
		transfer(amountSold, tradeLog, broker, currency, fromBorrowed, toOpen, dateOpen);
		fee(openCommission, broker, tradeLog, dateOpen);

		tradeLog.setDateOpen(dateOpen);
		tradeLog.setItemSold(itemSold);
		tradeLog.setTotalSold(amountSold);
		tradeLog.setOpenCommission(openCommission);

		tradeLogRepo.save(tradeLog);

		logger.debug("finish");
	}

	/**
	 * Selling a long position (closing or partially selling).
	 * <p>
	 * Creates a cash flow record for each transfer.
	 * <p>
	 * We transfer the sum of the open position back to the trade account (maybe partially).<br>
	 * We transfer the selling commission from the trade account<br>
	 * We transfer the outcome to or from the trade account<br>
	 * We update the trade log record with the selling data. If item sold number is equal to the bought item number,
	 * then we close the position.
	 *
	 * @param itemSold          number of items sold
	 * @param amountSold        amount sold
	 * @param sellingCommission selling commission
	 * @param dateTime          date and time of selling
	 * @param broker            broker
	 * @param tradeLog          trade record related to the transfer (required)
	 */
	public void sell(long itemSold, double amountSold, double sellingCommission, LocalDateTime dateTime,
	                 Broker broker, TradeLog tradeLog) {
		logger.debug("start");
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType tradeType   = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType openType    = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType outcomeType = accountTypeRepo.findCashAccountTypeByName("outcome");

		if (tradeType == null || openType == null || outcomeType == null) {
			throw new IllegalStateException("Account types are not found");
		}

		double openAmount    = tradeLog.getTotalBought();
		double itemBought    = tradeLog.getItemBought();
		double avgPriceOpen  = openAmount / itemBought;
		double amountToClose = itemSold * avgPriceOpen;
		double outcome       = amountSold - amountToClose;

		Currency tradeCurrency = tradeLog.getCurrency();

		// we transfer the sum of the open position back to the trade account (maybe partially, based on the number of
		// items sold)
		transfer(amountToClose, tradeLog, broker, tradeCurrency, openType, tradeType, dateTime);
		// we transfer the selling commission from the trade account
		fee(sellingCommission, broker, tradeLog, dateTime);
		// we transfer the outcome to or from the trade account
		if (outcome > 0) { // we have profit
			transfer(outcome, tradeLog, broker, tradeCurrency, outcomeType, tradeType, dateTime);
		} else if (outcome < 0) { // we have loss
			transfer(-outcome, tradeLog, broker, tradeCurrency, tradeType, outcomeType, dateTime);
		}

		// we update the trade log record
		double totalSold          = tradeLog.getTotalSold() == null ? 0 : tradeLog.getTotalSold();
		double closeCommission    = tradeLog.getCloseCommission() == null ? 0 : tradeLog.getCloseCommission();
		long   itemSoldPreviously = tradeLog.getItemSold() == null ? 0 : tradeLog.getItemSold();

		if (itemSoldPreviously + itemSold > itemBought) {
			throw new IllegalStateException("Item sold number is greater than item bought number");
		}

		tradeLog.setTotalSold(totalSold + amountSold);
		tradeLog.setCloseCommission(closeCommission + sellingCommission);
		tradeLog.setItemSold(itemSoldPreviously + itemSold);

		if (tradeLog.getItemSold() == itemBought) {
			tradeLog.setDateClose(dateTime);
		}

		tradeLogRepo.save(tradeLog);

		logger.debug("finish");
	}

	/**
	 * Buying a long position (opening).
	 * <p>
	 * Transfers money from trade account to open account.<br>
	 * Transfers the commission from the trade account to fee account.<br>
	 * Updates the trade log record with the trade data.
	 * <p>
	 * Creates a cash flow record for each transfer.
	 *
	 * @param itemBought     item bought
	 * @param amountBought   amount bought
	 * @param openCommission open commission
	 * @param dateOpen       date and time of buying
	 * @param broker         broker
	 * @param currency       currency
	 * @param tradeLog       trade record related to the transfer (required)
	 */
	public void buy(long itemBought,
	                double amountBought,
	                double openCommission,
	                LocalDateTime dateOpen,
	                Broker broker,
	                Currency currency,
	                TradeLog tradeLog) {
		logger.debug("start");
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType toOpen    = accountTypeRepo.findCashAccountTypeByName("open");
		if (fromTrade == null || toOpen == null) {
			throw new IllegalStateException("Account types are not found");
		}
		transfer(amountBought, tradeLog, broker, currency, fromTrade, toOpen, tradeLog.getDateOpen());
		fee(openCommission, broker, tradeLog, dateOpen);

		tradeLog.setItemBought(itemBought);
		tradeLog.setTotalBought(amountBought);
		tradeLog.setOpenCommission(openCommission);
		tradeLog.setDateOpen(dateOpen);

		tradeLogRepo.save(tradeLog);

		logger.debug("finish");
	}

	/**
	 * Buying a short position (closing or partially buying).
	 * <p>
	 * Creates a cash flow record for each transfer.
	 * <p>
	 * We transfer the sum of the open position back to the trade account (maybe partially).<br>
	 * We transfer the buying commission from the trade account.<br>
	 * We transfer the borrowing commission from the trade account.<br>
	 * We transfer the outcome to or from the trade account.<br>
	 * We update the trade log record with the trade data. If item bought is equal to item sold, we close the position.
	 *
	 * @param itemBought          item bought
	 * @param amountBought        amount bought
	 * @param buyingCommission    buying commission
	 * @param borrowingCommission borrowing commission
	 * @param dateTime            date and time of buying
	 * @param broker              broker
	 * @param tradeLog            trade record related to the transfer (required)
	 */
	public void buyShort(long itemBought,
	                     double amountBought,
	                     double buyingCommission,
	                     double borrowingCommission,
	                     LocalDateTime dateTime,
	                     Broker broker,
	                     TradeLog tradeLog) {
		logger.debug("start");
		if (tradeLog == null) {
			throw new IllegalArgumentException("Trade log record is required");
		}
		CashAccountType tradeType    = accountTypeRepo.findCashAccountTypeByName("trade");
		CashAccountType borrowedType = accountTypeRepo.findCashAccountTypeByName("borrowed");
		CashAccountType openType     = accountTypeRepo.findCashAccountTypeByName("open");
		CashAccountType outcomeType  = accountTypeRepo.findCashAccountTypeByName("outcome");

		if (tradeType == null || borrowedType == null || openType == null || outcomeType == null) {
			throw new IllegalStateException("Account types are not found");
		}

		double openAmount    = tradeLog.getTotalSold();
		double itemSold      = tradeLog.getItemSold();
		double avgPriceOpen  = openAmount / itemSold;
		double amountToClose = avgPriceOpen * itemBought;
		double outcome       = amountToClose - amountBought;

		Currency tradeCurrency = tradeLog.getCurrency();

		// we transfer the sum of the open position back to the borrowed account (maybe partially)
		transfer(amountToClose, tradeLog, broker, tradeCurrency, openType, borrowedType, dateTime);
		// we transfer the buying commission from the trade account
		fee(buyingCommission, broker, tradeLog, dateTime);
		// we transfer the borrowing commission from the trade account
		fee(borrowingCommission, broker, tradeLog, dateTime);
		// we transfer the outcome to or from the trade account
		if (outcome > 0) { // we have profit
			transfer(outcome, tradeLog, broker, tradeCurrency, outcomeType, tradeType, dateTime);
		} else if (outcome < 0) { // we have loss
			transfer(-outcome, tradeLog, broker, tradeCurrency, tradeType, outcomeType, dateTime);
		}

		double totalBought          = tradeLog.getTotalBought() == null ? 0 : tradeLog.getTotalBought();
		double closeCommission      = tradeLog.getCloseCommission() == null ? 0 : tradeLog.getCloseCommission();
		long   itemBoughtPreviously = tradeLog.getItemBought() == null ? 0 : tradeLog.getItemBought();

		if (itemBoughtPreviously + itemBought > itemSold) {
			throw new IllegalStateException("Item bought number is greater than item sold number");
		}

		tradeLog.setTotalBought(totalBought + amountBought);
		tradeLog.setCloseCommission(closeCommission + buyingCommission);
		tradeLog.setBrokerInterest(borrowingCommission);
		tradeLog.setItemBought(itemBoughtPreviously + itemBought);

		if (tradeLog.getItemBought() == itemSold) {
			tradeLog.setDateClose(dateTime);
		}

		tradeLogRepo.save(tradeLog);

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

		return capital + getOpenPositionsUSD();
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

	public double getOpenPositionsUSD() throws JsonProcessingException {
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
		double capital      = getCapital();
		double maxVolumeUSD = capital * depositPc / 100.0;
		double priceUSD     = currencyRateService.convertToUSD(currency.getId(), price, LocalDate.now());
		return (long) (maxVolumeUSD / priceUSD);
	}

	public EvalToFitRecord evalToFit(EvalInFitDTO evalDTO,
	                                 Currency currency,
	                                 Double atr,
	                                 Double stopLoss,
	                                 Double takeProfit,
	                                 double price,
	                                 double capital) throws JsonProcessingException {
		double            volumePc;
		EvalOutDTO        eval;
		long              items       = getMaxItems(currency, evalDTO.depositPc(), price);
		final Broker      broker      = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker      ticker      = tickerRepo.getReferenceById(evalDTO.tickerId());
		final String      brokerName  = broker.getName();
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(ticker.getCurrency());
		EvalInDTO         dto;
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


	/**
	 * Evaluate a position to fit the risk limits
	 * <p>
	 * The calculation of price, stop loss and take profit is based on a level and ATR and
	 * fits American stock market conditions only. For other markets the calculation must be
	 * adjusted.
	 * <p>
	 * @param evalDTO the input data
	 * @return the evaluation data
	 * @throws JsonProcessingException if a currency rate is not available
	 */
	public EvalOutFitDTO evalToFit(EvalInFitDTO evalDTO) throws JsonProcessingException {
		final int shortC     = evalDTO.isShort() ? -1 : 1;
		Double    levelPrice = evalDTO.levelPrice();
		Double    atr        = evalDTO.atr();
		Currency  currency   = tickerRepo.getReferenceById(evalDTO.tickerId()).getCurrency();
		double    price      = levelPrice + (shortC * 0.05);
		double    capital    = getCapital();
		// calculated stop loss is 0.2% of the level price (for US stocks)
		double stopLoss =
				evalDTO.stopLoss() == null ? levelPrice - (shortC * levelPrice / 100 * 0.2) : evalDTO.stopLoss();
		// calculated take profit is 70% of ATR
		double takeProfit = price + (shortC * atr * 0.7);

		EvalToFitRecord evalToFitRecord;

		if (evalDTO.technicalStop()) { // if we want a technical stop instead of a calculated one
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
		long            ms      = System.currentTimeMillis();
		double          rrPC    = 0;
		double          capital = getCapital();
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
		final Broker      broker      = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker      ticker      = tickerRepo.getReferenceById(evalDTO.tickerId());
		final String      brokerName  = broker.getName();
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(ticker.getCurrency());
		final double      capital     = getCapital();
		return eval(evalDTO, brokerName, currencyDTO, capital);
	}


	public EvalOutDTO eval(EvalInDTO evalDTO,
	                       String brokerName,
	                       CurrencyDTO currencyDTO,
	                       double capital) throws JsonProcessingException {
		final long   items           = evalDTO.items();
		final double priceOpen       = evalDTO.price();
		final double volume          = priceOpen * items;
		final double openCommission  = estimatedCommission(brokerName, currencyDTO, items, volume).getAmount();
		final double takeProfit      = evalDTO.takeProfit();
		final double closeCommission = estimatedCommission(brokerName, currencyDTO, items, takeProfit * items).getAmount();
		final double stopLoss        = evalDTO.stopLoss();
		final int    shortC          = evalDTO.isShort() ? -1 : 1;

		final double breakEven = getBreakEven(shortC, brokerName, currencyDTO, items, priceOpen);

		final double profit = abs(takeProfit - priceOpen) * items;

		final double loss = abs(stopLoss - breakEven) * items;

		final double taxes = getTaxes(profit);

		final double outcomeExp = abs(profit - openCommission - closeCommission - taxes);

		final double riskRewardPc = round(loss / outcomeExp * 100, 2);

		final double riskPc = round(getRiskPc(items,
		                                      stopLoss,
		                                      breakEven,
		                                      capital,
		                                      evalDTO.date(),
		                                      currencyDTO
		), 2);

		final double gainPc = round(outcomeExp / volume * 100, 2);

		return new EvalOutDTO(outcomeExp, gainPc, openCommission, riskPc, riskRewardPc, breakEven, volume);
	}

	/**
	 * Estimation of risk per trade in percent.
	 * Calculates a maximum loss in percent to a risk base.
	 * The risk base is the capital or less.
	 *
	 * @param items       number of securities
	 * @param stopLoss    stop loss
	 * @param breakEven   break even point
	 * @param capital     the capital
	 * @param openDate    the date of opening position
	 * @param currencyDTO currency
	 * @return estimated risk per trade in percent
	 * @throws JsonProcessingException exception
	 */
	public double getRiskPc(long items,
	                        double stopLoss,
	                        double breakEven,
	                        double capital,
	                        LocalDate openDate,
	                        CurrencyDTO currencyDTO) throws JsonProcessingException {
		final long   currencyId = currencyDTO.getId();
		final double riskBase   = getRiskBase(capital);

		final double breakEvenUSD =
				currencyRateService.convertToUSD(
						currencyId,
						breakEven,
						openDate);

		final double stopLossUSD =
				currencyRateService.convertToUSD(
						currencyId,
						stopLoss,
						openDate);

		final double breakEvenSum = items * breakEvenUSD;

		final double sumLoss = items * stopLossUSD;

		final double losses = abs(breakEvenSum - sumLoss);

		return losses / riskBase * 100.0;
	}

	/**
	 * Estimation of a break even point
	 *
	 * @param shortC      -1 is short, +1 if long
	 * @param brokerName  a broker name
	 * @param currencyDTO currency
	 * @param items       number of securities
	 * @param priceOpen   the price of opening the trade
	 * @return estimated break even point
	 */
	public double getBreakEven(int shortC,
	                           String brokerName,
	                           CurrencyDTO currencyDTO,
	                           long items,
	                           double priceOpen) {
		double sumOpen         = items * priceOpen;
		double openCommission  = estimatedCommission(brokerName, currencyDTO, items, sumOpen).getAmount();
		double sumClose        = sumOpen;
		double closeCommission = estimatedCommission(brokerName, currencyDTO, items, sumClose).getAmount();
		double taxes           = getTaxes(abs(sumClose - sumOpen));

		while (abs(sumClose - sumOpen) < openCommission + closeCommission + taxes) {
			sumClose = sumClose + (shortC * 0.01);
			closeCommission = estimatedCommission(brokerName, currencyDTO, items, sumClose).getAmount();
			taxes = getTaxes(abs(sumClose - sumOpen));
		}

		return round(sumClose / items, 2);
	}

	private double getTaxes(double sum) {
		return sum * 0.1; // ИПН
	}

	/**
	 * Calculates estimated commission.
	 * Since this value is used in a trade assessment, we use the currency
	 * of the trade, although in fact the commission is always paid in
	 * the currency of a broker.
	 *
	 * @param brokerName  the name of the broker
	 * @param currencyDTO the currency of the trade
	 * @param items       the number of securities
	 * @param sum         the sum of the trade
	 * @return the commission
	 */
	public Commission estimatedCommission(String brokerName, CurrencyDTO currencyDTO, long items, Double sum) {
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
			} else {
				throw new IllegalArgumentException("Unsupported currency for Interactive broker");
			}
		} else {
			throw new IllegalArgumentException("Unsupported broker");
		}
		return new Commission(fixed, round(fly, 2), round(amount, 2));
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
		CashAccountType tradeType      = accountTypeRepo.findCashAccountTypeByName("trade");

		CashAccountType from = amount > 0 ? correctionType : tradeType;
		CashAccountType to   = amount > 0 ? tradeType : correctionType;

		transfer(abs(amount), null, broker, currency, from, to, LocalDateTime.now());
	}
}


