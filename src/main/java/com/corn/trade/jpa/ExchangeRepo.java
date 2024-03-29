package com.corn.trade.jpa;

import com.corn.trade.entity.Exchange;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

public class ExchangeRepo extends JpaRepo<Exchange, Long> {
	public ExchangeRepo() {
		super(Exchange.class);
	}

	public Optional<Exchange> findExchange(String exchangeName) throws DBException {
		try {
			return executeInsideEntityManager(em -> {
				TypedQuery<Exchange> query = em.createQuery("SELECT e FROM Exchange e where e.name=:name", Exchange.class);
				query.setParameter("name", exchangeName);
				return Optional.of(query.getSingleResult());
			});
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (PersistenceException e) {
			throw new DBException(e);
		}
	}
}
