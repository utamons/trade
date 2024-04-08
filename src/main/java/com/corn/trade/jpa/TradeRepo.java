package com.corn.trade.jpa;

import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Trade;
import com.corn.trade.type.TradeStatus;

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
}
