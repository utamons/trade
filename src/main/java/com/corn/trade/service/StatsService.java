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
import java.util.ArrayList;
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

	public static Map<LocalDate, List<TradeLog>> getTradesPerDay(List<TradeLog> trades) {
		Map<LocalDate, List<TradeLog>> tradesPerDayMap = new HashMap<>();

		for (TradeLog tradeLog : trades) {
			LocalDate      openDate     = tradeLog.getDateOpen().toLocalDate();
			List<TradeLog> tradesPerDay = tradesPerDayMap.computeIfAbsent(openDate, k -> new ArrayList<>());
			tradesPerDay.add(tradeLog);
		}

		return tradesPerDayMap;
	}

	public StatsData getStats(TimePeriod timePeriod, Long currencyId, Long brokerId) throws JsonProcessingException {
		final Broker                             broker = brokerId == null ? null : brokerRepo.getReferenceById(brokerId);
		final Pair<LocalDateTime, LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);

		List<TradeLog> trades              = getTrades(timePeriod, currencyId, brokerId);
		List<TradeLog> tradesAllCurrencies = getTrades(timePeriod, null, brokerId);

		Map<LocalDate, List<TradeLog>> tradesPerDayMap    = getTradesPerDay(trades);
		Map<LocalDate, Integer>        tradeCountPerDay   = countTradesOpenedPerDay(tradesPerDayMap);
		Map<LocalDate, Double>         tradesVolumePerDay = countTradesVolumePerDay(tradesPerDayMap);
		long                           partials           = trades.stream().filter(TradeLog::isPartial).count();

		List<LocalDateTime> weekdaysBetween = TimePeriodConverter.getWeekdaysBetween(period.left(), period.right());
		long                tradesPerDayMax = tradeCountPerDay.values().stream().max(Integer::compareTo).orElse(0);
		double tradesPerDayAvg =
				tradeCountPerDay.values().stream().mapToDouble(Integer::intValue).sum() / tradeCountPerDay.size();

		double volumePerTradeMax = trades.stream().mapToDouble(TradeLog::getVolume).max().orElse(0.0);
		double volumePerTradeAvg = trades.stream().mapToDouble(TradeLog::getVolume).sum() / trades.size();
		double volumePerDayAvg =
				tradesVolumePerDay.values().stream().mapToDouble(Double::doubleValue).sum() / tradesVolumePerDay.size();
		double volumePerDayMax     = tradesVolumePerDay.values().stream().max(Double::compareTo).orElse(0.0);
		double volumeAll           = trades.stream().mapToDouble(TradeLog::getVolume).sum();
		double volumeAllCurrencies = tradesAllCurrencies.stream().mapToDouble(TradeLog::getVolume).sum();
		double capitalAvg          = capitalAvg(broker, weekdaysBetween);
		double capitalTurnover     = volumeAllCurrencies / capitalAvg * 100.0;


		double commissions            = trades.stream().mapToDouble(TradeLog::getFees).sum();
		double commissionsPerTradeAvg = commissions / trades.size();

		/*
		   К профитам есть вопросы. Если они в денежном выражении, то как быть с разными валютами?
		 */

		double profitPerTradeAvg = trades.stream().mapToDouble(TradeLog::getProfit).sum() / trades.size();
		double profitPerDayAvg   = trades.stream().mapToDouble(TradeLog::getProfit).sum() / tradesPerDayMap.size();
		double profitPerDayMax = tradesPerDayMap.values().stream()
		                                        .mapToDouble(tradesPerDay -> tradesPerDay.stream()
		                                                                                 .mapToDouble(TradeLog::getProfit)
		                                                                                 .sum())
		                                        .max()
		                                        .orElse(0.0);
		double profitPartialsAvg = trades.stream().filter(trade -> trade.isPartial() && trade.isClosed())
		                                 .mapToDouble(TradeLog::getProfit)
		                                 .sum() / partials;

		double profitSinglesAvg = trades.stream().filter(trade -> !trade.isPartial() && trade.isClosed())
		                                .mapToDouble(TradeLog::getProfit)
		                                .sum() / (trades.size() - partials);


		return aStatsData()
				.withTrades((long) trades.size())
				.withDayWithTradesDayRatio(round((double) tradeCountPerDay.size() / weekdaysBetween.size() * 100.0))
				.withPartials(partials)
				.withTradesPerDayMax(tradesPerDayMax)
				.withTradesPerDayAvg(round(tradesPerDayAvg))

				.withVolumePerDayMax(round(volumePerDayMax))
				.withVolumePerDayAvg(round(volumePerDayAvg))
				.withVolumePerTradeMax(round(volumePerTradeMax))
				.withVolumePerTradeAvg(round(volumePerTradeAvg))
				.withCapitalTurnover(round(capitalTurnover))
				.withVolume(round(volumeAll))

				.withCommissionsPerTradeAvg(round(commissionsPerTradeAvg))
				.withCommissions(round(commissions))

				.withProfitPerTradeAvg(round(profitPerTradeAvg))
				.withProfitPerDayAvg(round(profitPerDayAvg))
				.withProfitPerDayMax(round(profitPerDayMax))
				.withProfitPartialsAvg(round(profitPartialsAvg))
				.withProfitSinglesAvg(round(profitSinglesAvg))

				.withCapital(cashService.getCapital(null, null))
				.build();
	}

	private Double capitalAvg(Broker broker, List<LocalDateTime> weekdaysBetween) throws JsonProcessingException {
		double capitalSum = 0.0;

		for (LocalDateTime weekday : weekdaysBetween) {
			capitalSum += cashService.getCapital(broker, weekday);
		}

		return capitalSum / weekdaysBetween.size();
	}

	private Map<LocalDate, Double> countTradesVolumePerDay(Map<LocalDate, List<TradeLog>> tradesPerDayMap) {
		Map<LocalDate, Double> tradesVolumePerDay = new HashMap<>();

		for (Map.Entry<LocalDate, List<TradeLog>> entry : tradesPerDayMap.entrySet()) {
			double volume = entry.getValue().stream().mapToDouble(TradeLog::getVolume).sum();
			tradesVolumePerDay.put(entry.getKey(), volume);
		}

		return tradesVolumePerDay;
	}

	private Map<LocalDate, Integer> countTradesOpenedPerDay(Map<LocalDate, List<TradeLog>> tradesPerDayMap) {
		Map<LocalDate, Integer> tradeCountPerDay = new HashMap<>();

		for (Map.Entry<LocalDate, List<TradeLog>> entry : tradesPerDayMap.entrySet()) {
			tradeCountPerDay.put(entry.getKey(), entry.getValue().size());
		}

		return tradeCountPerDay;
	}

	public List<TradeLog> getTrades(TimePeriod timePeriod, Long currencyId, Long brokerId) {
		final Pair<LocalDateTime, LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);

		CriteriaBuilder         cb           = entityManager.getCriteriaBuilder();
		CriteriaQuery<TradeLog> cq           = cb.createQuery(TradeLog.class);
		Root<TradeLog>          tradeLogRoot = cq.from(TradeLog.class);

		Predicate predicate;

		if (currencyId == null && brokerId == null)
			predicate = cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right());
		else if (currencyId != null && brokerId == null)
			predicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                   cb.equal(tradeLogRoot.get("currency").get("id"), currencyId));
		else if (currencyId == null)
			predicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                   cb.equal(tradeLogRoot.get("broker").get("id"), brokerId));
		else
			predicate = cb.and(cb.between(tradeLogRoot.get("dateOpen"), period.left(), period.right()),
			                   cb.equal(tradeLogRoot.get("currency").get("id"), currencyId),
			                   cb.equal(tradeLogRoot.get("broker").get("id"), brokerId));

		cq.where(predicate);
		return entityManager.createQuery(cq).getResultList();
	}

	public MoneyStateDTO getMoneyState() throws JsonProcessingException {
		double            capital        = cashService.getCapital(null, null);
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

	public BrokerStatsDTO getBrokerStats(Long brokerId) throws JsonProcessingException {
		Broker broker = brokerRepo.getReferenceById(brokerId);

		double capital = cashService.getCapital(broker, null);

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
