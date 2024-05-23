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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class JpaRepo<T, ID extends Serializable> implements IRepo {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JpaRepo.class);
	private final        Class<T>         entityClass;

	protected EntityManager entityManager;

	public JpaRepo(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	@Override
	public void withEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	public void closeEntityManager() {
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
		entityManager = null;
	}

	protected <R> R executeInsideEntityManager(Function<EntityManager, R> function) {
		if (entityManager != null) {
			return function.apply(entityManager);
		}
		EntityManager em = JpaUtil.getEntityManager();
		try {
			em.getTransaction().begin();
			R result = function.apply(em);
			em.getTransaction().commit();
			return result;
		} catch (Exception e) {
			log.error("Error executing inside entity manager", e);
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public Optional<T> findById(ID id) {
		T result = executeInsideEntityManager(em -> em.find(entityClass, id));
		return Optional.ofNullable(result);
	}

	public void update(T entity) {
		executeInsideEntityManager(em -> {
			em.merge(entity);
			return null;
		});
	}

	public T getById(ID id) {
		return executeInsideEntityManager(em -> em.find(entityClass, id));
	}

	public T save(T entity) {
		return executeInsideEntityManager(em -> {
			em.persist(entity);
			return entity;
		});
	}

	public List<T> findAll() {
		return executeInsideEntityManager(em -> {
			TypedQuery<T> query = em.createQuery("SELECT e FROM " + entityClass.getName() + " e", entityClass);
			return query.getResultList();
		});
	}

	public void delete(T entity) {
		executeInsideEntityManager(em -> {
			T mergedEntity = em.merge(entity);
			em.remove(mergedEntity);
			return null;
		});
	}
}
