/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

	public List<Trade> findAllOpen() {
		return executeInsideEntityManager(em -> em.createQuery("select a from Trade a where a.status = :status", Trade.class)
		                                        .setParameter("status", TradeStatus.OPEN.name())
		                                        .getResultList());
	}
}
