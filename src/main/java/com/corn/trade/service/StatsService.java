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
		final Integer totalTrades = tradeLogs.size();
		final Integer shorts = (int) tradeLogs.stream().filter(TradeLog::isShort).count();
		final Integer longs = (int) tradeLogs.stream().filter(TradeLog::isLong).count();

		final Double volumeUSD = tradeLogs.stream().mapToDouble(t-> {
			try {
				return currencyRateService.convertToUSD(
						t.getCurrency().getId(),
						t.getVolume(),
						LocalDate.now()
				);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return 0;
			}
		}).sum();

		final Double profitUSD = tradeLogs.stream().mapToDouble(t-> {
			try {
				return currencyRateService.convertToUSD(
						t.getCurrency().getId(),
						t.getProfit(),
						LocalDate.now()
				);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return 0;
			}
		}).sum();

		final Double performance = profitUSD / volumeUSD * 100.0;

		final Double capital = cashService.getCapital();

		return null;
	}
}
