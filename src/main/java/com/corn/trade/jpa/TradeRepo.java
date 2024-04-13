package com.corn.trade.jpa;

import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Trade;
import com.corn.trade.type.TradeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TradeRepo extends JpaRepo<Trade, Long> {
	public TradeRepo() {
		super(Trade.class);
	}

	public Trade getOpenTrade(Asset asset) {
		String query = "select a from Trade a where a.asset = :asset and a.status = :status";
		List<Trade> trades = executeInsideEntityManager(em -> em.createQuery(query, Trade.class)
		                                                        .setParameter("asset", asset)
		                                                        .setParameter("status", TradeStatus.OPEN.name())
		                                                        .getResultList());
		if (trades.size() > 1) {
			throw new IllegalStateException("More than one open trade found for asset " + asset.getName());
		}
		return trades.isEmpty() ? null : trades.get(0);
	}

	public long countClosedForToday() {
		return executeInsideEntityManager(em -> em.createQuery(
				                                          "SELECT count(t) FROM Trade t where t.closedAt >= :date1 and t" +
				                                          ".closedAt < :date2 and t.status = :status",
				                                          Long.class)
		                                          .setParameter("date1", LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
		                                          .setParameter("date2", LocalDateTime.of(LocalDate.now(), LocalTime.MAX))
		                                          .setParameter("status", TradeStatus.CLOSED.name())
		                                          .getSingleResult());
	}
}
