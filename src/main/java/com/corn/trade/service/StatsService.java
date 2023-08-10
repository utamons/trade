package com.corn.trade.service;

import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.dto.StatsData;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.TradeLog;
import com.corn.trade.mapper.TimePeriodConverter;
import com.corn.trade.repository.CashAccountRepository;
import com.corn.trade.repository.CashAccountTypeRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.corn.trade.util.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.corn.trade.service.CashService.OUTCOME;

@Service
public class StatsService {
	private final TradeLogRepository tradeLogRepo;
	private final CashService               cashService;
	private final CashAccountTypeRepository accountTypeRepo;

	private final CashAccountRepository     cashAccountRepo;

	private final CurrencyRateService currencyRateService;

	public StatsService(TradeLogRepository tradeLogRepo, CashService cashService,
	                    CashAccountTypeRepository accountTypeRepo, CashAccountRepository cashAccountRepo,
	                    CurrencyRateService currencyRateService) {
		this.tradeLogRepo = tradeLogRepo;
		this.cashService = cashService;
		this.accountTypeRepo = accountTypeRepo;
		this.cashAccountRepo = cashAccountRepo;
		this.currencyRateService = currencyRateService;
	}

	public StatsData getStats(TimePeriod timePeriod) {
		final Pair<LocalDateTime,LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);
		final Pair<LocalDateTime,LocalDateTime> periodPrev = TimePeriodConverter.getPreviousTimeRange(period);

		List<TradeLog> tradeLogs = tradeLogRepo.findAllClosedByPeriod(period.left(), period.right());
		List<TradeLog> prevTradeLogs = tradeLogRepo.findAllClosedByPeriod(periodPrev.left(), periodPrev.right());

		StatsData stats = getStats(tradeLogs);
		StatsData prevStats = getStats(prevTradeLogs);
		stats.setPrevData(prevStats);

		return stats;
	}

	public StatsData getStats(List<TradeLog> tradeLogs) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public MoneyStateDTO getMoneyState() throws JsonProcessingException {
		double          capital     = cashService.getCapital();
		CashAccountType outcomeType = accountTypeRepo.findCashAccountTypeByName(OUTCOME);
		List<CashAccount> accounts = cashAccountRepo.findAllByType(outcomeType);
		double sumOutcomesUSD = 0.0;

		for (CashAccount account : accounts) {
			double outcome    = cashService.getAccountTotal(account);
			double outcomeUSD = currencyRateService.convertToUSD(account.getCurrency().getId(), outcome, LocalDate.now());
			sumOutcomesUSD += Math.abs(outcomeUSD);
		}

		double profit = capital == 0 ? 0.0 : sumOutcomesUSD / capital * 100.0;

		double riskBase = cashService.getRiskBase(capital);

		return new MoneyStateDTO(capital, profit, riskBase);
	}

	public BrokerStatsDTO getStats(Long brokerId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
