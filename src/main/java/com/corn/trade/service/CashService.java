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

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.round;
import static java.lang.Math.abs;

@SuppressWarnings("DuplicatedCode")
@Service
@Transactional
public class CashService {
	public static final  String                    START                        = "start";
	public static final  String                    FINISH                       = "finish";
	public static final  String                    TRADE                        = "trade";
	public static final  String                    FEE                          = "fee";
	public static final  String                    TRADE_LOG_RECORD_IS_REQUIRED = "Trade log record is required";
	public static final  String                    BORROWED                    = "borrowed";
	public static final  String                    ACCOUNT_TYPES_ARE_NOT_FOUND = "Account types are not found";
	public static final  String                    OPEN                        = "open";
	public static final  String                    OUTCOME                     = "outcome";
	public static final  String                    FREEDOM_FN                  = "FreedomFN";
	public static final  String                    INTERACTIVE                 = "Interactive";
	public static final  String                    USD                         = "USD";
	public static final  String                    KZT                         = "KZT";
	public static final  String                    EUR                         = "EUR";
	private static final Logger                    logger                      =
			LoggerFactory.getLogger(CashService.class);
	public static final  String                    INCOME                      = "income";
	public static final String WITHDRAWAL = "withdraw";
	private final        CashAccountRepository     accountRepo;
	private final        CashFlowRepository        cashFlowRepo;
	private final        BrokerRepository          brokerRepo;
	private final        CurrencyRepository        currencyRepo;
	private final        CashAccountTypeRepository accountTypeRepo;
	private final        TickerRepository          tickerRepo;

	private final TradeLogRepository  tradeLogRepo;
	private final CurrencyRateService currencyRateService;

	private final EntityManager entityManager;

	public CashService(CashAccountRepository accountRepo,
	                   CashFlowRepository cashFlowRepo,
	                   BrokerRepository brokerRepo,
	                   CurrencyRepository currencyRepo,
	                   CashAccountTypeRepository accountTypeRepo,
	                   TickerRepository tickerRepo,
	                   TradeLogRepository tradeLogRepo,
	                   CurrencyRateService currencyRateService,
	                   EntityManager entityManager) {
		this.accountRepo = accountRepo;
		this.cashFlowRepo = cashFlowRepo;
		this.brokerRepo = brokerRepo;
		this.currencyRepo = currencyRepo;
		this.accountTypeRepo = accountTypeRepo;
		this.tickerRepo = tickerRepo;
		this.tradeLogRepo = tradeLogRepo;
		this.currencyRateService = currencyRateService;
		this.entityManager = entityManager;
	}

	private static void prepareTradeLogQuery(LocalDateTime date,
	                                         Broker broker,
	                                         Root<TradeLog> tradeLogRoot,
	                                         CriteriaBuilder cb,
	                                         String position,
	                                         CriteriaQuery<CurrencySumDTO> cq,
	                                         Expression<Double> exp) {
		Join<TradeLog, Currency> currencyJoin = tradeLogRoot.join("currency");

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(tradeLogRoot.get("position"), position));

		if (date == null) { // get open positions at the moment
            predicates.add(cb.isNull(tradeLogRoot.get("dateClose")));
		} else {
			predicates.add(cb.lessThanOrEqualTo(tradeLogRoot.get("dateOpen"), date));
			predicates.add(cb.greaterThan(tradeLogRoot.get("dateClose"), date));
		}
		if (broker != null) {
			predicates.add(cb.equal(tradeLogRoot.get("broker"), broker));
		}

		Predicate predicate = cb.and(
				predicates.toArray(new Predicate[0])
		);

		cq.select(cb.construct(
				CurrencySumDTO.class,
				currencyJoin.get("id"),
				cb.sum(exp)
		));
		cq.where(predicate);
		cq.groupBy(currencyJoin);
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
		logger.debug(START);
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
		logger.debug(FINISH);
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
		logger.debug(START);
		Broker          broker     = brokerRepo.getReferenceById(transferDTO.brokerId());
		Currency        currency   = currencyRepo.getReferenceById(transferDTO.currencyId());
		CashAccountType toTrade    = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType fromIncome = accountTypeRepo.findCashAccountTypeByName(INCOME);

		CashAccount trade = transfer(
				transferDTO.amount(),
				null,
				broker,
				currency,
				fromIncome,
				toTrade,
				LocalDateTime.now());
		logger.debug(FINISH);
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
			throw new IllegalArgumentException("Broker is required");
		}
		if (currency == null) {
			throw new IllegalArgumentException("Currency is required");
		}
		if (fromType == null) {
			throw new IllegalArgumentException("From type is required");
		}
		if (toType == null) {
			throw new IllegalArgumentException("To type is required");
		}
		if (dateTime == null) {
			throw new IllegalArgumentException("Date and time are required");
		}
		if (transfer == 0) {
			throw new IllegalArgumentException("Transfer amount is zero. Nothing to do");
		}


		CashAccount from = getAccount(broker, currency, fromType);
		CashAccount to   = getAccount(broker, currency, toType);

		CashFlow cashFlow = new CashFlow(from, to, tradeLog, transfer, transfer, null, dateTime);
		cashFlowRepo.save(cashFlow);

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
		logger.debug(START);
		Broker          broker         = brokerRepo.getReferenceById(exchangeDTO.brokerId());
		Currency        currencyFrom   = currencyRepo.getReferenceById(exchangeDTO.currencyFromId());
		Currency        currencyTo     = currencyRepo.getReferenceById(exchangeDTO.currencyToId());
		CashAccountType tradeType      = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType conversionType = accountTypeRepo.findCashAccountTypeByName("conversion");

		CashAccount tradeFrom    = getAccount(broker, currencyFrom, tradeType);
		CashAccount tradeTo      = getAccount(broker, currencyTo, tradeType);
		CashAccount conversionTo = getAccount(broker, currencyTo, conversionType);
		double      transferFrom = exchangeDTO.amountFrom();
		double      transferTo   = exchangeDTO.amountTo();
		double      totalFrom    = getAccountTotal(tradeFrom);
		if (currencyFrom.getId() == currencyTo.getId()) {
			throw new IllegalArgumentException("Currencies are the same");
		}
		if (transferFrom > totalFrom) {
			throw new IllegalArgumentException("Not enough money to transfer");
		}
		double delta = transferFrom - transferTo;

		CashFlow cashFlow = new CashFlow(
				tradeFrom,
				tradeTo,
				null,
				transferFrom,
				transferFrom,
				null,
				LocalDateTime.now()
		);
		cashFlowRepo.save(cashFlow);

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

		logger.debug(FINISH);
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
		logger.debug(START);
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType toFee     = accountTypeRepo.findCashAccountTypeByName("fee");
		Currency        currency  = broker.getFeeCurrency();
		if (amount == 0.0) {
			throw new IllegalArgumentException("Fee amount is zero. Nothing to do");
		}
		if (currency == null) {
			throw new IllegalArgumentException("Broker fee currency is null");
		}
		if (fromTrade == null) {
			throw new IllegalStateException("Trade account type is null");
		}
		if (toFee == null) {
			throw new IllegalStateException("Fee account type is null");
		}

		transfer(amount, tradeLog, broker, currency, fromTrade, toFee, dateTime);
		logger.debug(FINISH);
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
		logger.debug(START);
		if (tradeLog == null) {
			throw new IllegalArgumentException(TRADE_LOG_RECORD_IS_REQUIRED);
		}
		CashAccountType fromBorrowed = accountTypeRepo.findCashAccountTypeByName(BORROWED);
		CashAccountType toOpen       = accountTypeRepo.findCashAccountTypeByName(OPEN);
		if (fromBorrowed == null || toOpen == null) {
			throw new IllegalStateException(ACCOUNT_TYPES_ARE_NOT_FOUND);
		}
		transfer(amountSold, tradeLog, broker, currency, fromBorrowed, toOpen, dateOpen);
		fee(openCommission, broker, tradeLog, dateOpen);

		tradeLog.setDateOpen(dateOpen);
		tradeLog.setItemSold(itemSold);
		tradeLog.setTotalSold(amountSold);
		tradeLog.setOpenCommission(openCommission);

		tradeLogRepo.save(tradeLog);

		logger.debug(FINISH);
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
		logger.debug(START);
		if (tradeLog == null) {
			throw new IllegalArgumentException(TRADE_LOG_RECORD_IS_REQUIRED);
		}
		CashAccountType tradeType   = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType openType    = accountTypeRepo.findCashAccountTypeByName(OPEN);
		CashAccountType outcomeType = accountTypeRepo.findCashAccountTypeByName(OUTCOME);

		if (tradeType == null || openType == null || outcomeType == null) {
			throw new IllegalStateException(ACCOUNT_TYPES_ARE_NOT_FOUND);
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
		long   partsClosed        = tradeLog.getPartsClosed();

		if (itemSoldPreviously + itemSold > itemBought) {
			throw new IllegalStateException("Item sold number is greater than item bought number");
		}

		tradeLog.setTotalSold(totalSold + amountSold);
		tradeLog.setCloseCommission(closeCommission + sellingCommission);
		tradeLog.setItemSold(itemSoldPreviously + itemSold);
		tradeLog.setPartsClosed(partsClosed + 1);

		if (tradeLog.getItemSold() == itemBought) {
			tradeLog.setDateClose(dateTime);
		}

		tradeLogRepo.save(tradeLog);

		logger.debug(FINISH);
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
		logger.debug(START);
		if (tradeLog == null) {
			throw new IllegalArgumentException(TRADE_LOG_RECORD_IS_REQUIRED);
		}
		CashAccountType fromTrade = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType toOpen    = accountTypeRepo.findCashAccountTypeByName(OPEN);
		if (fromTrade == null || toOpen == null) {
			throw new IllegalStateException(ACCOUNT_TYPES_ARE_NOT_FOUND);
		}
		transfer(amountBought, tradeLog, broker, currency, fromTrade, toOpen, tradeLog.getDateOpen());
		fee(openCommission, broker, tradeLog, dateOpen);

		tradeLog.setItemBought(itemBought);
		tradeLog.setTotalBought(amountBought);
		tradeLog.setOpenCommission(openCommission);
		tradeLog.setDateOpen(dateOpen);

		tradeLogRepo.save(tradeLog);

		logger.debug(FINISH);
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
		logger.debug(START);
		if (tradeLog == null) {
			throw new IllegalArgumentException(TRADE_LOG_RECORD_IS_REQUIRED);
		}
		CashAccountType tradeType    = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType borrowedType = accountTypeRepo.findCashAccountTypeByName(BORROWED);
		CashAccountType openType     = accountTypeRepo.findCashAccountTypeByName(OPEN);
		CashAccountType outcomeType  = accountTypeRepo.findCashAccountTypeByName(OUTCOME);

		if (tradeType == null || borrowedType == null || openType == null || outcomeType == null) {
			throw new IllegalStateException(ACCOUNT_TYPES_ARE_NOT_FOUND);
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
		if (borrowingCommission > 0)
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
		long   partsClosed          = tradeLog.getPartsClosed();

		if (itemBoughtPreviously + itemBought > itemSold) {
			throw new IllegalStateException("Item bought number is greater than item sold number");
		}

		tradeLog.setTotalBought(totalBought + amountBought);
		tradeLog.setCloseCommission(closeCommission + buyingCommission);
		tradeLog.setBrokerInterest(borrowingCommission);
		tradeLog.setItemBought(itemBoughtPreviously + itemBought);
		tradeLog.setPartsClosed(partsClosed + 1);

		if (tradeLog.getItemBought() == itemSold) {
			tradeLog.setDateClose(dateTime);
		}

		tradeLogRepo.save(tradeLog);

		logger.debug(FINISH);
	}

	/**
	 * Estimated overall capital.
	 * <p>
	 * Is used for statistics only. Risk assessment is made on a per-broker basis.
	 *
	 * @return estimated overall capital
	 * @throws JsonProcessingException if currency conversion fails
	 */
	public double getCapital(Broker broker, LocalDateTime localDateTime) throws JsonProcessingException {
		CashAccountType tradeType = accountTypeRepo.findCashAccountTypeByName(TRADE);
		List<CashAccount> accounts = broker == null ? accountRepo.findAllByType(tradeType) :
				accountRepo.findAllByBrokerAndType(broker, tradeType);
		double    capital = 0.0;
		LocalDate today   = LocalDate.now();
		for (CashAccount account : accounts) {
			double amount = getAccountTotal(account, localDateTime);
			capital += currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					amount,
					today);
		}

		return capital + openPositionsUSD(broker, localDateTime);
	}

	public double getRefills(Broker broker, LocalDateTime localDateTime) throws JsonProcessingException {
		CashAccountType fromIncome = accountTypeRepo.findCashAccountTypeByName(INCOME);
		List<CashAccount> accounts = broker == null ? accountRepo.findAllByType(fromIncome) :
				accountRepo.findAllByBrokerAndType(broker, fromIncome);
		double    refills = 0.0;
		LocalDate today   = LocalDate.now();
		for (CashAccount account : accounts) {
			double amount = Math.abs(getAccountTotal(account, localDateTime));
			refills += currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					amount,
					today);
		}

		return refills;
	}

	public double getWithdrawals(Broker broker, LocalDateTime localDateTime) throws JsonProcessingException {
		CashAccountType toWithdrawal = accountTypeRepo.findCashAccountTypeByName(WITHDRAWAL);
		List<CashAccount> accounts = broker == null ? accountRepo.findAllByType(toWithdrawal) :
				accountRepo.findAllByBrokerAndType(broker, toWithdrawal);
		double    withdrawals = 0.0;
		LocalDate today   = LocalDate.now();
		for (CashAccount account : accounts) {
			double amount = Math.abs(getAccountTotal(account, localDateTime));
			withdrawals += currencyRateService.convertToUSD(
					account.getCurrency().getId(),
					amount,
					today);
		}

		return withdrawals;
	}

	public double getAccountTotal(CashAccount account) {
		return getAccountTotal(account, null);
	}

	public double getAccountTotal(CashAccount account, LocalDateTime dateTime) {
		Double sumFrom = dateTime == null ? cashFlowRepo.getSumFromByAccount(account) :
				cashFlowRepo.getSumFromByAccountToDate(account, dateTime);
		Double sumTo = dateTime == null ? cashFlowRepo.getSumToByAccount(account) :
				cashFlowRepo.getSumToByAccountToDate(account, dateTime);
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

	public List<CurrencySumDTO> getOpenLongSums(Broker broker, LocalDateTime date) {
		CriteriaBuilder               cb           = entityManager.getCriteriaBuilder();
		CriteriaQuery<CurrencySumDTO> cq           = cb.createQuery(CurrencySumDTO.class);
		Root<TradeLog>                tradeLogRoot = cq.from(TradeLog.class);
		String                        position     = "long";
		Expression<Double>            exp          = cb.diff(tradeLogRoot.get("totalBought"), tradeLogRoot.get("risk"));

		prepareTradeLogQuery(date, broker, tradeLogRoot, cb, position, cq, exp);

		return entityManager.createQuery(cq).getResultList();
	}

	public List<CurrencySumDTO> getOpenShortRisks(Broker broker, LocalDateTime date) {
		CriteriaBuilder               cb           = entityManager.getCriteriaBuilder();
		CriteriaQuery<CurrencySumDTO> cq           = cb.createQuery(CurrencySumDTO.class);
		Root<TradeLog>                tradeLogRoot = cq.from(TradeLog.class);
		String                        position     = "short";
		Expression<Double>            exp          = tradeLogRoot.get("risk");

		prepareTradeLogQuery(date, broker, tradeLogRoot, cb, position, cq, exp);

		return entityManager.createQuery(cq).getResultList();
	}

	public double getRiskBase(double capital) {
		return Math.min(capital, 5000.0);
	}

	/**
	 * Estimated capital, stored in open positions in all brokers.
	 * <p>
	 * We take all long positions at maximum risk (all losses) and
	 * distract all maximum risks in short positions (all losses).
	 * <p>
	 * Thus, we get the capital that is stored in open positions in the worth case.
	 *
	 * @return the estimated capital, stored in open positions
	 * @throws JsonProcessingException if currency rate service fails.
	 */
	public double openPositionsUSD(Broker broker, LocalDateTime localDateTime) throws JsonProcessingException {
		List<CurrencySumDTO> opens      = getOpenLongSums(broker, localDateTime);
		List<CurrencySumDTO> shortRisks = getOpenShortRisks(broker, localDateTime);

		double    sum  = 0.0;
		LocalDate date = localDateTime == null ? LocalDate.now() : localDateTime.toLocalDate();

		for (CurrencySumDTO dto : opens) {
			sum += currencyRateService.convertToUSD(
					dto.currencyId(),
					dto.sum(),
					date);
		}

		for (CurrencySumDTO dto : shortRisks) {
			sum -= currencyRateService.convertToUSD(
					dto.currencyId(),
					dto.sum(),
					date);
		}

		return sum;
	}

	/**
	 * Calculates the maximum number of items per trade for the given capital.
	 *
	 * @param capital   capital
	 * @param depositPc deposit percentage per trade
	 * @param price     price
	 * @param currency  currency of the trade
	 * @return the maximum number of items per trade
	 * @throws JsonProcessingException if currency rate service fails.
	 */
	public long getMaxItems(double capital,
	                        double depositPc,
	                        double price,
	                        Currency currency) throws JsonProcessingException {
		double maxVolumeUSD = getRiskBase(capital) * depositPc / 100.0;
		double priceUSD     = currencyRateService.convertToUSD(currency.getId(), price, LocalDate.now());
		return (long) (maxVolumeUSD / priceUSD);
	}

	/**
	 * Calculates an optimal number of items for a trade based on
	 * allowed risk, risk/reward ratio and maximum volume per trade.
	 *
	 * @param evalDTO    evaluation parameters
	 * @param currency   currency of the trade
	 * @param atr        average true range
	 * @param stopLoss   stop loss
	 * @param takeProfit take profit
	 * @param price      price
	 * @param capital    capital by a broker
	 * @return calculation results
	 * @throws JsonProcessingException if currency rate service fails.
	 */
	public EvalToFitRecord evalToFit(EvalInFitDTO evalDTO,
	                                 Currency currency,
	                                 Double atr,
	                                 Double stopLoss,
	                                 Double takeProfit,
	                                 double price,
	                                 double capital) throws JsonProcessingException {
		double     volumePc;
		EvalOutDTO eval;
		long       items = getMaxItems(capital, evalDTO.depositPc(), price, currency);
		if (items == 0) {
			throw new IllegalStateException("No money for trading in the currency " + currency.getName().trim() + "!");
		}
		final Broker      broker      = brokerRepo.getReferenceById(evalDTO.brokerId());
		final Ticker      ticker      = tickerRepo.getReferenceById(evalDTO.tickerId());
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
			eval = eval(dto, broker.getName(), currencyDTO, capital);
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
	 *
	 * @param evalDTO the input data
	 * @return the evaluation data
	 * @throws JsonProcessingException if a currency rate is not available
	 */
	public EvalOutFitDTO evalToFit(EvalInFitDTO evalDTO) throws JsonProcessingException {
		final int shortC     = evalDTO.isShort() ? -1 : 1;
		Double    levelPrice = evalDTO.levelPrice();
		Double    atr        = evalDTO.atr();
		Currency  currency   = tickerRepo.getReferenceById(evalDTO.tickerId()).getCurrency();
		Broker    broker     = brokerRepo.getReferenceById(evalDTO.brokerId());
		double    price      = levelPrice + (shortC * 0.06);
		double    capital    = getCapital(broker, null);
		if (capital == 0.0) {
			throw new IllegalStateException("No capital for broker " + broker.getName());
		}
		// calculated stop loss is 0.2% of the level price (for US stocks)
		double stopLoss =
				evalDTO.stopLoss() == null ? levelPrice - (shortC * levelPrice / 100 * 0.2) : evalDTO.stopLoss();
		// calculated take profit is 65% of ATR
		double takeProfit = price + (shortC * atr * 0.65);

		EvalToFitRecord evalToFitRecord;

		if (evalDTO.technicalStop()) { // if we want a technical stop instead of a calculated one
			stopLoss = getTechnicalStop(broker, evalDTO, shortC, atr, currency, price, stopLoss, takeProfit);
			stopLoss = stopLoss + (shortC * 0.01);
		}
		evalToFitRecord = evalToFit(evalDTO, currency, atr, stopLoss, takeProfit, price, capital);

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
				evalToFitRecord.eval.risk(),
				evalToFitRecord.eval.riskRewardPc(),
				evalToFitRecord.volumePc
		);
	}

	private double getTechnicalStop(Broker broker,
									EvalInFitDTO evalDTO,
	                                int shortC,
	                                Double atr,
	                                Currency currency,
	                                double price,
	                                double stopLoss,
	                                double takeProfit) throws JsonProcessingException {
		EvalToFitRecord evalToFitRecord;
		long            ms      = System.currentTimeMillis();
		double          rrPC    = 0;
		double          capital = getCapital(broker, null);
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
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(ticker.getCurrency());
		final double      capital     = getCapital(broker, null);
		return eval(evalDTO, broker.getName(), currencyDTO, capital);
	}

	public EvalOutDTO eval(EvalInDTO evalDTO,
	                       String brokerName,
	                       CurrencyDTO currencyDTO,
	                       double capital) throws JsonProcessingException {
		final long   items          = evalDTO.items();
		final double priceOpen      = evalDTO.price();
		final double volume         = priceOpen * items;
		final double openCommission = estimatedCommission(brokerName, currencyDTO, items, volume).amount();
		final double takeProfit     = evalDTO.takeProfit();
		final double closeCommission =
				estimatedCommission(brokerName, currencyDTO, items, takeProfit * items).amount();
		final double stopLoss = evalDTO.stopLoss();
		final int    shortC   = evalDTO.isShort() ? -1 : 1;

		final double breakEven = getBreakEven(shortC, brokerName, currencyDTO, items, priceOpen);

		final double grossProfit = abs(takeProfit - priceOpen) * items;

		final double risk = round(abs(stopLoss - breakEven) * items, 2);

		final double netOutcome = round(abs(grossProfit - openCommission - closeCommission), 2);

		final double riskRewardPc = round(risk / netOutcome * 100, 2);

		final double riskPc = round(getRiskPc(risk, capital, evalDTO.date(), currencyDTO), 2);

		final double gainPc = round(netOutcome / volume * 100, 2);

		return new EvalOutDTO(netOutcome,
		                      gainPc,
		                      openCommission + closeCommission,
		                      risk,
		                      riskPc,
		                      riskRewardPc,
		                      breakEven,
		                      volume);
	}

	/**
	 * Estimation of risk per trade in percent to a risk base.
	 * The risk base is the capital or less.
	 *
	 * @param risk        risk in money
	 * @param capital     the capital
	 * @param openDate    the date of opening position
	 * @param currencyDTO currency
	 * @return estimated risk per trade in percent
	 * @throws JsonProcessingException exception
	 */
	public double getRiskPc(double risk,
	                        double capital,
	                        LocalDate openDate,
	                        CurrencyDTO currencyDTO) throws JsonProcessingException {
		final long   currencyId = currencyDTO.id();
		final double riskBase   = getRiskBase(capital);
		final double riskUSD    = currencyRateService.convertToUSD(currencyId, risk, openDate);

		return riskUSD / riskBase * 100.0;
	}

	/**
	 * Estimation of a break even point.
	 *
	 * @param shortC        -1 is short, +1 if long
	 * @param brokerName    a broker name
	 * @param tradeCurrency currency of the trade
	 * @param items         number of securities
	 * @param priceOpen     the price of opening the trade
	 * @return estimated break even point
	 */
	public double getBreakEven(int shortC,
	                           String brokerName,
	                           CurrencyDTO tradeCurrency,
	                           long items,
	                           double priceOpen) {
		double sumOpen         = items * priceOpen;
		double openCommission  = estimatedCommission(brokerName, tradeCurrency, items, sumOpen).amount();
		double sumClose        = sumOpen;
		double closeCommission = estimatedCommission(brokerName, tradeCurrency, items, sumClose).amount();
		double taxes           = getTaxes(abs(sumClose - sumOpen));

		while (abs(sumClose - sumOpen) < openCommission + closeCommission + taxes) {
			sumClose = sumClose + (shortC * 0.01);
			closeCommission = estimatedCommission(brokerName, tradeCurrency, items, sumClose).amount();
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
		double fixed = 0.0;
		double fly   = 0.0;
		double amount;

		if (brokerName.equals(FREEDOM_FN)) {
			if (currencyDTO.name().equals(KZT)) {
				amount = fly = sum / 100.0 * 0.085;
			} else {
				fixed = 1.2;
				fly = sum / 100.0 * 0.5 + items * 0.012;
				amount = fixed + fly;
			}
		} else if (brokerName.equals(INTERACTIVE)) {
			if (currencyDTO.name().equals(USD)) {
				double max = items * sum / 100.0;
				double min = items * 0.005;
				amount = Math.min(max, min);
				amount = amount < 1 ? 1 : amount;
			} else {
				throw new IllegalArgumentException("The currency " +
				                                   currencyDTO.name() +
				                                   " is not supported for trades in Interactive broker yet");
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
		CashAccountType tradeType      = accountTypeRepo.findCashAccountTypeByName(TRADE);

		CashAccountType from = amount > 0 ? correctionType : tradeType;
		CashAccountType to   = amount > 0 ? tradeType : correctionType;

		transfer(abs(amount), null, broker, currency, from, to, LocalDateTime.now());
	}

	public record EvalToFitRecord(long items, double volumePc, EvalOutDTO eval) {
	}
}


