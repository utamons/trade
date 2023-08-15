package com.corn.trade.service;

import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.dto.CashAccountOutDTO;
import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.dto.StatsData;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.TradeLog;
import com.corn.trade.mapper.CashAccountMapper;
import com.corn.trade.mapper.TimePeriodConverter;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.CashAccountRepository;
import com.corn.trade.repository.CashAccountTypeRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.corn.trade.util.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.corn.trade.dto.StatsData.aStatsData;
import static com.corn.trade.service.CashService.*;
import static com.corn.trade.util.Util.round;

@Service
public class StatsService {
	private final TradeLogRepository        tradeLogRepo;
	private final CashService               cashService;
	private final CashAccountTypeRepository accountTypeRepo;
	private final CashAccountRepository     cashAccountRepo;
	private final CurrencyRateService       currencyRateService;
	private final BrokerRepository          brokerRepo;

	private final EntityManager entityManager;

	public StatsService(TradeLogRepository tradeLogRepo, CashService cashService,
	                    CashAccountTypeRepository accountTypeRepo, CashAccountRepository cashAccountRepo,
	                    CurrencyRateService currencyRateService, BrokerRepository brokerRepo, EntityManager entityManager) {
		this.tradeLogRepo = tradeLogRepo;
		this.cashService = cashService;
		this.accountTypeRepo = accountTypeRepo;
		this.cashAccountRepo = cashAccountRepo;
		this.currencyRateService = currencyRateService;
		this.brokerRepo = brokerRepo;
		this.entityManager = entityManager;
	}

	public static Map<LocalDate, Integer> countTradesOpenedPerDay(List<TradeLog> trades,
	                                                              Pair<LocalDateTime, LocalDateTime> period) {
		Map<LocalDate, Integer> tradeCountPerDay = new HashMap<>();

		LocalDateTime currentDateTime = period.left();

		while (currentDateTime.isBefore(period.right().plusMinutes(1))) {
			LocalDate currentDate = currentDateTime.toLocalDate();
			int       count       = 0;

			for (TradeLog trade : trades) {
				LocalDateTime tradeOpenDate      = trade.getDateOpen();
				LocalDate     tradeOpenLocalDate = tradeOpenDate.toLocalDate();
				if (tradeOpenLocalDate.equals(currentDate)) {
					count++;

				}
			}

			if (count > 0)
				tradeCountPerDay.put(currentDate, count);
			currentDateTime = currentDateTime.plusDays(1);
		}

		return tradeCountPerDay;
	}

	public StatsData getStats(TimePeriod timePeriod, Long currencyId, Long brokerId) throws JsonProcessingException {
		final Pair<LocalDateTime, LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);

		List<TradeLog> trades = getTrades(timePeriod, currencyId, brokerId);

		Map<LocalDate, Integer> tradeCountPerDay = countTradesOpenedPerDay(trades, period);
		long daysBetween = TimePeriodConverter.countWeekdaysBetween(period.left(), period.right());
		long tradesPerDayMax = tradeCountPerDay.values().stream().max(Integer::compareTo).orElse(0);
		double tradesPerDayAvg =  tradeCountPerDay.values().stream().mapToDouble(Integer::intValue).sum() / tradeCountPerDay.size();

		return aStatsData()
				.withTrades((long) trades.size())
				.withDayWithTradesDayRatio(round((double) tradeCountPerDay.size() / daysBetween * 100.0))
				.withPartials(partials(trades))
				.withTradesPerDayMax(tradesPerDayMax)
				.withTradesPerDayAvg(round(tradesPerDayAvg))
				.withCapital(cashService.getCapital())
				.build();
	}

	private long partials(List<TradeLog> trades) {
		return trades.stream().filter(trade -> trade.getPartsClosed() > 1).count();
	}

	public List<TradeLog> getTrades(TimePeriod timePeriod, Long currencyId, Long brokerId) {
		final Pair<LocalDateTime, LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);

		CriteriaBuilder         cb           = entityManager.getCriteriaBuilder();
		CriteriaQuery<TradeLog> cq           = cb.createQuery(TradeLog.class);
		Root<TradeLog>          tradeLogRoot = cq.from(TradeLog.class);

		Predicate datePredicate;

		if (currencyId == null && brokerId == null)
			datePredicate = cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right());
		else if (currencyId != null && brokerId == null)
			datePredicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                       cb.equal(tradeLogRoot.get("currency").get("id"), currencyId));
		else if (currencyId == null)
			datePredicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                       cb.equal(tradeLogRoot.get("broker").get("id"), brokerId));
		else
			datePredicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                       cb.equal(tradeLogRoot.get("currency").get("id"), currencyId),
			                       cb.equal(tradeLogRoot.get("broker").get("id"), brokerId));

		cq.where(datePredicate);
		return entityManager.createQuery(cq).getResultList();
	}

	public MoneyStateDTO getMoneyState() throws JsonProcessingException {
		double            capital        = cashService.getCapital();
		CashAccountType   outcomeType    = accountTypeRepo.findCashAccountTypeByName(OUTCOME);
		CashAccountType   feeType        = accountTypeRepo.findCashAccountTypeByName(FEE);
		List<CashAccount> accounts       = cashAccountRepo.findAllByType(outcomeType);
		double            sumOutcomesUSD = 0.0;
		double            sumFeesUSD     = 0.0;

		for (CashAccount account : accounts) {
			double outcome    = cashService.getAccountTotal(account);
			double outcomeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), outcome, LocalDate.now());
			sumOutcomesUSD += Math.abs(outcomeUSD);
		}

		for (CashAccount account : cashAccountRepo.findAllByType(feeType)) {
			double fee    = cashService.getAccountTotal(account);
			double feeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), fee, LocalDate.now());
			sumFeesUSD += Math.abs(feeUSD);
		}

		double profit = capital == 0 ? 0.0 : (sumOutcomesUSD - sumFeesUSD) / capital * 100.0;

		return new MoneyStateDTO(capital, profit);
	}

	public BrokerStatsDTO getStats(Long brokerId) throws JsonProcessingException {
		Broker broker = brokerRepo.getReferenceById(brokerId);

		double capital = cashService.getCapital(broker);

		double riskBase = cashService.getRiskBase(capital);

		CashAccountType outcomeType = accountTypeRepo.findCashAccountTypeByName(OUTCOME);
		CashAccountType tradeType   = accountTypeRepo.findCashAccountTypeByName(TRADE);
		CashAccountType feeType     = accountTypeRepo.findCashAccountTypeByName(FEE);

		List<CashAccount> outcomeAccounts = cashAccountRepo.findAllByBrokerAndType(broker, outcomeType);
		List<CashAccount> feeAccounts     = cashAccountRepo.findAllByBrokerAndType(broker, feeType);
		List<CashAccount> tradeAccounts   = cashAccountRepo.findAllByBrokerAndType(broker, tradeType);

		double sumOutcomesUSD = 0.0;
		double sumFeesUSD     = 0.0;

		for (CashAccount account : outcomeAccounts) {
			double outcome    = cashService.getAccountTotal(account);
			double outcomeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), outcome, LocalDate.now());
			sumOutcomesUSD += Math.abs(outcomeUSD);
		}

		for (CashAccount account : feeAccounts) {
			double fee    = cashService.getAccountTotal(account);
			double feeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), fee, LocalDate.now());
			sumFeesUSD += Math.abs(feeUSD);
		}

		long open = tradeLogRepo.opensCountByBroker(broker);

		List<CashAccountOutDTO> tradeAccountsOutDTO = tradeAccounts.stream().map(acc -> {
			double total = cashService.getAccountTotal(acc);
			return CashAccountMapper.toOutDTO(CashAccountMapper.toDTO(acc), total);
		}).toList();


		return new BrokerStatsDTO(tradeAccountsOutDTO, sumOutcomesUSD - sumFeesUSD, open, riskBase);
	}
}
