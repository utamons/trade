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
