package com.corn.trade.service;

import com.corn.trade.dto.StatsData;
import com.corn.trade.entity.TradeLog;
import com.corn.trade.mapper.TimePeriodConverter;
import com.corn.trade.repository.TradeLogRepository;
import com.corn.trade.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {
	private final TradeLogRepository tradeLogRepo;

	public StatsService(TradeLogRepository tradeLogRepo) {
		this.tradeLogRepo = tradeLogRepo;
	}

	public StatsData getStats(TimePeriod timePeriod) {
		final Pair<LocalDateTime,LocalDateTime> period = TimePeriodConverter.getDateTimeRange(timePeriod);
		final Pair<LocalDateTime,LocalDateTime> periodPrev = TimePeriodConverter.getPreviousTimeRange(period);

		List<TradeLog> tradeLogs = tradeLogRepo.findAllClosedByPeriod(period.getLeft(), period.getRight());
		List<TradeLog> prevTradeLogs = tradeLogRepo.findAllClosedByPeriod(periodPrev.getLeft(), periodPrev.getRight());

		StatsData stats = getStats(tradeLogs);
		StatsData prevStats = getStats(prevTradeLogs);
		stats.setPrevData(prevStats);

		return stats;
	}

	public StatsData getStats(List<TradeLog> tradeLogs) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
