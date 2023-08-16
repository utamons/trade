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
		final LocalDate                          now    = LocalDate.now();

		double capitalStart = cashService.getCapital(broker, period.left());

		List<TradeLog> trades              = getTrades(timePeriod, currencyId, brokerId);
		List<TradeLog> tradesAllCurrencies = getTrades(timePeriod, null, brokerId);
		List<TradeLog> partials 		  = trades.stream().filter(TradeLog::isPartial).toList();
		List<TradeLog> singles 			  = trades.stream().filter(trade -> !trade.isPartial()).toList();

		Map<LocalDate, List<TradeLog>> tradesPerDayMap    = getTradesPerDay(trades);
		Map<LocalDate, Integer>        tradeCountPerDay   = countTradesOpenedPerDay(tradesPerDayMap);
		Map<LocalDate, Double>         tradesVolumePerDay = countTradesVolumePerDay(tradesPerDayMap);
		long                           partialsCount           = trades.stream().filter(TradeLog::isPartial).count();

		List<LocalDateTime> weekdaysBetween = TimePeriodConverter.getWeekdaysBetween(period.left(), period.right());

		long tradesPerDayMax = tradeCountPerDay.values().stream().max(Integer::compareTo).orElse(0);

		double tradesPerDayAvg =
				tradeCountPerDay.values().stream().mapToDouble(Integer::intValue).sum() / tradeCountPerDay.size();

		double volumePerTradeMax = 0.0;

		for (TradeLog trade : trades) {
			double convertedVolume = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getVolume(), now);
			volumePerTradeMax = Math.max(volumePerTradeMax, convertedVolume);
		}

		double volumePerTradeAvg = 0.0;

		for (TradeLog trade : trades) {
			double convertedVolume = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getVolume(), now);
			volumePerTradeAvg += convertedVolume;
		}

		volumePerTradeAvg /= trades.size();

		double volumePerDayAvg =
				tradesVolumePerDay.values().stream().mapToDouble(Double::doubleValue).sum() / tradesPerDayMap.size();

		double volumePerDayMax = tradesVolumePerDay.values().stream().max(Double::compareTo).orElse(0.0);

		double volumeAll = tradesVolumePerDay.values().stream().mapToDouble(Double::doubleValue).sum();

		double volumeAllCurrencies = 0.0;

		for (TradeLog trade : tradesAllCurrencies) {
			double convertedVolume = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getVolume(), now);
			volumeAllCurrencies += convertedVolume;
		}

		double capitalAvg      = capitalAvg(broker, weekdaysBetween);
		double capitalTurnover = volumeAllCurrencies / capitalAvg * 100.0;

		double commissions = 0.0;

		for (TradeLog trade : trades) {
			double convertedCommission =
					currencyRateService.convertToUSD(trade.getBroker().getFeeCurrency().getId(), trade.getFees(), now);
			commissions += convertedCommission;
		}

		double commissionsPerTradeAvg = commissions / trades.size();

		double profitPerTradeAvg = 0.0;

		for (TradeLog trade : trades) {
			double convertedProfit = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			profitPerTradeAvg += convertedProfit;
		}

		profitPerTradeAvg /= trades.size();

		double profitPerDayAvg = 0.0;

		for (TradeLog trade : trades) {
			double convertedProfit = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			profitPerDayAvg += convertedProfit;
		}

		profitPerDayAvg /= tradesPerDayMap.size();

		double profitPerDayMax = 0.0;

		for (List<TradeLog> tradeLogs : tradesPerDayMap.values()) {
			double convertedProfit = 0.0;
			for (TradeLog trade : tradeLogs) {
				convertedProfit += currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			}
			profitPerDayMax = Math.max(profitPerDayMax, convertedProfit);
		}

		double profitPartialsAvg = 0.0;

		for (TradeLog trade : partials) {
			double convertedProfit = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			profitPartialsAvg += convertedProfit;
		}

		profitPartialsAvg /= partials.size();

		double profitSinglesAvg = 0.0;

		for (TradeLog trade : singles) {
			double convertedProfit = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			profitSinglesAvg += convertedProfit;
		}

		profitSinglesAvg /= singles.size();

		double profitAll = 0.0;

		for (TradeLog trade : trades) {
			double convertedProfit = currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getProfit(), now);
			profitAll += convertedProfit;
		}

		double profitVolumePc = profitAll / volumeAll * 100.0;

		double profitCapitalPc = profitAll / capitalStart * 100.0;


		return aStatsData()
				.withTrades((long) trades.size())
				.withDayWithTradesDayRatio(round((double) tradeCountPerDay.size() / weekdaysBetween.size() * 100.0))
				.withPartials(partialsCount)
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
				.withProfit(round(profitAll))
				.withProfitVolumePc(round(profitVolumePc))
				.withProfitCapitalPc(round(profitCapitalPc))

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

	private Map<LocalDate, Double> countTradesVolumePerDay(Map<LocalDate, List<TradeLog>> tradesPerDayMap) throws JsonProcessingException {
		Map<LocalDate, Double> tradesVolumePerDay = new HashMap<>();

		for (Map.Entry<LocalDate, List<TradeLog>> entry : tradesPerDayMap.entrySet()) {
			double volume = 0.0;

			for (TradeLog trade : entry.getValue()) {
				double convertedVolume =
						currencyRateService.convertToUSD(trade.getCurrency().getId(), trade.getVolume(), LocalDate.now());
				volume += convertedVolume;
			}

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
