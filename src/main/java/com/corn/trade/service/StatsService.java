package com.corn.trade.service;

import com.corn.trade.dto.StatsData;
import com.corn.trade.entity.TradeLog;
import com.corn.trade.mapper.TimePeriodConverter;
import com.corn.trade.repository.TradeLogRepository;
import com.corn.trade.util.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {

	private final CashService cashService;
	private final CurrencyRateService currencyRateService;
	private final TradeLogRepository tradeLogRepo;

	public StatsService(CashService cashService, CurrencyRateService currencyRateService, TradeLogRepository tradeLogRepo) {
		this.cashService = cashService;
		this.currencyRateService = currencyRateService;
		this.tradeLogRepo = tradeLogRepo;
	}

	public StatsData getStats(TimePeriod timePeriod) throws JsonProcessingException {
		final Pair<LocalDateTime,LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);
		final Pair<LocalDateTime,LocalDateTime> periodPrev = TimePeriodConverter.getPreviousTimeRange(period);

		List<TradeLog> tradeLogs = tradeLogRepo.findAllClosedByPeriod(period.getLeft(), period.getRight());
		List<TradeLog> prevTradeLogs = tradeLogRepo.findAllClosedByPeriod(periodPrev.getLeft(), periodPrev.getRight());

		StatsData stats = getStats(tradeLogs);
		StatsData prevStats = getStats(prevTradeLogs);
		stats.setPrevData(prevStats);

		return stats;
	}

	public StatsData getStats(List<TradeLog> tradeLogs) throws JsonProcessingException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
