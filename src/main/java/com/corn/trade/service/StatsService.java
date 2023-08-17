package com.corn.trade.service;

import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.dto.CashAccountOutDTO;
import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.dto.StatsData;
import com.corn.trade.entity.*;
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
import java.time.Duration;
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

	public static Map<LocalDate, List<Trade>> getTradesPerDay(List<Trade> trades) {
		Map<LocalDate, List<Trade>> tradesPerDayMap = new HashMap<>();

		for (Trade trade : trades) {
			LocalDate   openDate     = trade.dateOpen().toLocalDate();
			List<Trade> tradesPerDay = tradesPerDayMap.computeIfAbsent(openDate, k -> new ArrayList<>());
			tradesPerDay.add(trade);
		}

		return tradesPerDayMap;
	}

	public StatsData getStats(TimePeriod timePeriod, Long currencyId, Long brokerId) throws JsonProcessingException {
		final Broker                             broker = brokerId == null ? null : brokerRepo.getReferenceById(brokerId);
		final Pair<LocalDateTime, LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);
		final LocalDate                          now    = LocalDate.now();

		double capitalStart = cashService.getCapital(broker, period.left());
		double capitalEnd   = cashService.getCapital(broker, period.right());

		List<Trade> tradesAllCurrencies = getTradesAllCurrencies(timePeriod, brokerId, now);

		List<Trade> trades;

		if (currencyId == null)
			trades = tradesAllCurrencies;
		else
			trades = tradesAllCurrencies.stream().filter(trade -> trade.currency().getId() == currencyId).toList();

		List<Trade> partials = trades.stream().filter(Trade::isPartial).toList();
		List<Trade> singles  = trades.stream().filter(trade -> !trade.isPartial()).toList();

		Map<LocalDate, List<Trade>> tradesPerDayMap    = getTradesPerDay(trades);
		Map<LocalDate, Integer>     tradeCountPerDay   = countTradesOpenedPerDay(tradesPerDayMap);
		Map<LocalDate, Double>      tradesVolumePerDay = countTradesVolumePerDay(tradesPerDayMap);
		long                        partialsCount      = trades.stream().filter(Trade::isPartial).count();
		long positiveCount = trades.stream().filter(trade -> trade.profit > 0).count();
		long negativeCount = trades.stream().filter(trade -> trade.loss > 0).count();

		List<LocalDateTime> weekdaysBetween = TimePeriodConverter.getWeekdaysBetween(period.left(), period.right());

		long tradesPerDayMax = tradeCountPerDay.values().stream().max(Integer::compareTo).orElse(0);

		double tradesPerDayAvg = tradeCountPerDay.isEmpty() ? 0.0 :
				tradeCountPerDay.values().stream().mapToDouble(Integer::intValue).sum() / tradeCountPerDay.size();

		double volumePerTradeMax = trades.stream().mapToDouble(Trade::volume).max().orElse(0.0);

		double volumePerTradeAvg = trades.isEmpty() ? 0.0 :
				trades.stream().mapToDouble(Trade::volume).sum() / trades.size();

		double volumePerDayAvg = tradesPerDayMap.isEmpty() ? 0.0 :
				tradesVolumePerDay.values().stream().mapToDouble(Double::doubleValue).sum() / tradesPerDayMap.size();

		double volumePerDayMax = tradesVolumePerDay.values().stream().max(Double::compareTo).orElse(0.0);

		double volumeAll = trades.stream().mapToDouble(Trade::volume).sum();

		double volumeAllCurrencies = tradesAllCurrencies.stream().mapToDouble(Trade::volume).sum();

		double capitalAvg      = capitalAvg(broker, weekdaysBetween);
		double capitalTurnover = capitalAvg == 0 ? 0.0 : volumeAllCurrencies / capitalAvg * 100.0;

		double commissions = trades.stream().mapToDouble(Trade::fees).sum();

		double commissionsPerTradeAvg = trades.isEmpty()? 0.0 : commissions / trades.size();

		double profitPerTradeAvg = positiveCount == 0? 0.0 : trades.stream().mapToDouble(Trade::profit).sum() / positiveCount;

		double profitPerDayAvg = tradesPerDayMap.isEmpty()? 0.0 : tradesPerDayMap.values().stream().mapToDouble(tradesPerDay ->
				                                                                       tradesPerDay.stream()
				                                                                                   .mapToDouble(Trade::profit)
				                                                                                   .sum()
		).sum() / tradesPerDayMap.size();

		double profitPerDayMax = tradesPerDayMap.values().stream().mapToDouble(tradesPerDay ->
				                                                                       tradesPerDay.stream()
				                                                                                   .mapToDouble(Trade::profit)
				                                                                                   .sum()
		).max().orElse(0.0);

		double profitPartialsAvg = partials.isEmpty() ? 0.0 :partials.stream().mapToDouble(Trade::profit).sum() / partials.size();

		double profitSinglesAvg = singles.isEmpty() ? 0.0 : singles.stream().mapToDouble(Trade::profit).sum() / singles.size();

		double profitAll = trades.stream().mapToDouble(Trade::profit).sum();

		double lossAll = trades.stream().mapToDouble(Trade::loss).sum();

		double profitVolumePc = volumeAll == 0 ? 0.0 : profitAll / volumeAll * 100.0;

		double profitCapitalPc = capitalStart == 0? 0.0 : profitAll / capitalStart * 100.0;

		double lossPerTradeAvg = trades.isEmpty() ? 0.0 : trades.stream().mapToDouble(Trade::loss).sum() / trades.size();
		double lossPerTradeMax = trades.stream().mapToDouble(Trade::loss).max().orElse(0.0);

		double riskRewardRatioAvg = positiveCount == 0 ? 0.0 :
				trades.stream().filter(t -> t.profit > 0).mapToDouble(Trade::riskRewardRatio).sum() / positiveCount * 100.0;

		double riskRewardRatioMax =
				trades.stream().filter(t -> t.profit > 0).mapToDouble(Trade::riskRewardRatio).max().orElse(0.0) * 100.0;

		double winRate = trades.isEmpty() ? 0.0 : positiveCount / (double) trades.size() * 100.0;

		double slippageAvg = trades.isEmpty() ? 0.0 :trades.stream().mapToDouble(Trade::slippage).sum() / trades.size();

		double takeDeltaAvg = positiveCount == 0 ? 0.0 : trades.stream().filter(t -> t.profit > 0).mapToDouble(Trade::takeDelta).sum() / positiveCount;

		double stopDeltaAvg = negativeCount == 0 ? 0.0 : trades.stream().filter(t -> t.loss > 0).mapToDouble(Trade::stopDelta).sum() / negativeCount;

		double capitalChange = capitalEnd - capitalStart;

		double refillsStart = cashService.getRefills(broker, period.left().minus(Duration.ofMinutes(1)));
		double refillsEnd = cashService.getRefills(broker, period.right());

		double refills = refillsEnd - refillsStart;

		double withdrawalsStart = cashService.getWithdrawals(broker, period.left().minus(Duration.ofMinutes(1)));
		double withdrawalsEnd = cashService.getWithdrawals(broker, period.right());

		double withdrawals = withdrawalsEnd - withdrawalsStart;


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

				.withLoss(round(lossAll))
				.withLossPerTradeAvg(round(lossPerTradeAvg))
				.withLossPerTradeMax(round(lossPerTradeMax))

				.withRiskRewardRatioAvg(round(riskRewardRatioAvg))
				.withRiskRewardRatioMax(round(riskRewardRatioMax))
				.withWinRate(round(winRate))
				.withSlippageAvg(round(slippageAvg))
				.withTakeDeltaAvg(round(takeDeltaAvg))
				.withStopDeltaAvg(round(stopDeltaAvg))

				.withCapital(capitalEnd)
				.withRefills(round(refills))
				.withWithdrawals(round(withdrawals))
				.withCapitalChange(round(capitalChange))
				.build();
	}

	private List<Trade> getTradesAllCurrencies(TimePeriod timePeriod,
	                                           Long brokerId,
	                                           LocalDate now) throws JsonProcessingException {
		List<Trade> tradesAllCurrencies = new ArrayList<>();

		for (TradeLog tradeLog : getTrades(timePeriod, null, brokerId)) {
			double convertedProfit =
					currencyRateService.convertToUSD(tradeLog.getCurrency().getId(), tradeLog.getProfit(), now);
			double convertedLoss =
					currencyRateService.convertToUSD(tradeLog.getCurrency().getId(), tradeLog.getLoss(), now);
			double convertedFees =
					currencyRateService.convertToUSD(tradeLog.getBroker().getFeeCurrency().getId(), tradeLog.getFees(),
					                                 now);
			double convertedVolume =
					currencyRateService.convertToUSD(tradeLog.getCurrency().getId(), tradeLog.getVolume(), now);
			Trade trade = new Trade(tradeLog.getCurrency(),
			                        tradeLog.getBroker().getFeeCurrency(),
			                        tradeLog.getDateOpen(),
			                        convertedVolume,
			                        convertedProfit,
			                        convertedLoss,
			                        convertedFees,
			                        tradeLog.isPartial(),
			                        tradeLog.isLong(),
			                        tradeLog.getRiskRewardRatio(),
			                        tradeLog.getSlippage(),
			                        tradeLog.getTakeDelta(),
			                        tradeLog.getStopDelta()
			);
			tradesAllCurrencies.add(trade);
		}
		return tradesAllCurrencies;
	}

	private Double capitalAvg(Broker broker, List<LocalDateTime> weekdaysBetween) throws JsonProcessingException {
		double capitalSum = 0.0;

		for (LocalDateTime weekday : weekdaysBetween) {
			capitalSum += cashService.getCapital(broker, weekday);
		}

		return capitalSum / weekdaysBetween.size();
	}

	private Map<LocalDate, Double> countTradesVolumePerDay(Map<LocalDate, List<Trade>> tradesPerDayMap) {
		Map<LocalDate, Double> tradesVolumePerDay = new HashMap<>();

		for (Map.Entry<LocalDate, List<Trade>> entry : tradesPerDayMap.entrySet()) {
			double volume = entry.getValue().stream().mapToDouble(t -> t.volume).sum();
			tradesVolumePerDay.put(entry.getKey(), volume);
		}

		return tradesVolumePerDay;
	}

	private Map<LocalDate, Integer> countTradesOpenedPerDay(Map<LocalDate, List<Trade>> tradesPerDayMap) {
		Map<LocalDate, Integer> tradeCountPerDay = new HashMap<>();

		for (Map.Entry<LocalDate, List<Trade>> entry : tradesPerDayMap.entrySet()) {
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
		double            capital      = cashService.getCapital(null, null);
		CashAccountType   outcomeType  = accountTypeRepo.findCashAccountTypeByName(OUTCOME);
		CashAccountType   feeType      = accountTypeRepo.findCashAccountTypeByName(FEE);
		List<CashAccount> accounts     = cashAccountRepo.findAllByType(outcomeType);
		List<CashAccount> feeAccounts  = cashAccountRepo.findAllByType(feeType);
		OutcomesFees      outcomesFees = getOutcomesFees(accounts, feeAccounts);

		double profit = capital == 0 ? 0.0 : (outcomesFees.sumOutcomesUSD() - outcomesFees.sumFeesUSD()) / capital * 100.0;

		return new MoneyStateDTO(capital, profit);
	}

	private OutcomesFees getOutcomesFees(List<CashAccount> accounts,
	                                     List<CashAccount> feeAccounts) throws JsonProcessingException {
		double sumOutcomesUSD = 0.0;
		double sumFeesUSD     = 0.0;

		for (CashAccount account : accounts) {
			double outcome    = cashService.getAccountTotal(account);
			double outcomeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), outcome, LocalDate.now());
			sumOutcomesUSD -= outcomeUSD;
		}

		for (CashAccount account : feeAccounts) {
			double fee    = cashService.getAccountTotal(account);
			double feeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), fee, LocalDate.now());
			sumFeesUSD += Math.abs(feeUSD);
		}
		return new OutcomesFees(sumOutcomesUSD, sumFeesUSD);
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

		OutcomesFees outcomesFees = getOutcomesFees(outcomeAccounts, feeAccounts);

		long open = tradeLogRepo.opensCountByBroker(broker);

		List<CashAccountOutDTO> tradeAccountsOutDTO = tradeAccounts.stream().map(acc -> {
			double total = cashService.getAccountTotal(acc);
			return CashAccountMapper.toOutDTO(CashAccountMapper.toDTO(acc), total);
		}).toList();


		return new BrokerStatsDTO(tradeAccountsOutDTO,
		                          outcomesFees.sumOutcomesUSD - outcomesFees.sumFeesUSD,
		                          open,
		                          riskBase);
	}

	private record OutcomesFees(double sumOutcomesUSD, double sumFeesUSD) {
	}

	public record Trade(
			Currency currency, Currency feeCurrency, LocalDateTime dateOpen,
			double volume, double profit, double loss, double fees, boolean isPartial, boolean isLong,
			double riskRewardRatio, double slippage, double takeDelta, double stopDelta
	) {
	}
}
