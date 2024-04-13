package com.corn.trade.jpa;

import com.corn.trade.entity.PnlEntity;
import liquibase.exception.DatabaseException;

import java.time.LocalDate;
import java.util.List;

public class PnlRepo extends JpaRepo<PnlEntity, Long> {
	public PnlRepo() {
		super(PnlEntity.class);
	}

	public Double countForDateRange(LocalDate dateFrom, LocalDate dateTo) {
		return executeInsideEntityManager(em -> em.createQuery(
				                                          "SELECT sum(p.value) FROM PnlEntity p where p.dateAt >= " +
				                                          ":dateFrom" +
				                                          " and p" +
				                                          ".dateAt <= :dateTo",
				                                          Double.class)
		                                          .setParameter("dateFrom", dateFrom)
		                                          .setParameter("dateTo", dateTo)
		                                          .getSingleResult());
	}

	public PnlEntity getForDate(LocalDate date) throws DatabaseException {
		List<PnlEntity> entities =
				executeInsideEntityManager(em -> em.createQuery(
						                                   "SELECT p FROM PnlEntity p where p.dateAt =" +
						                                   " " +
						                                   ":date",
						                                   PnlEntity.class)
				                                   .setParameter("date", date)
				                                   .getResultList());
		if (entities.size() > 1) {
			throw new DatabaseException("More than one PnL found for date " + date);
		}
		return entities.isEmpty() ? null : entities.get(0);
	}
}
