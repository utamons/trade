package com.corn.trade.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class JpaRepo<T, ID extends Serializable> {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JpaRepo.class);
	private final        Class<T>         entityClass;

	protected EntityManager entityManager;

	public JpaRepo(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public void withEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void closeEntityManager() {
		if (entityManager != null) {
			entityManager.close();
			entityManager = null;
		}
	}

	protected <R> R executeInsideEntityManager(Function<EntityManager, R> function) {
		if (entityManager != null) {
			return function.apply(entityManager);
		}
		try (EntityManager em = JpaUtil.getEntityManager()) {
			em.getTransaction().begin();
			R result = function.apply(em);
			em.getTransaction().commit();
			return result;
		}
	}

	protected <R> R executeWithEntityManager(Function<EntityManager, R> function) {
		if (entityManager != null) {
			return function.apply(entityManager);
		}
		try (EntityManager em = JpaUtil.getEntityManager()) {
			R result = function.apply(em);
			return result;
		}
	}

	public Optional<T> findById(ID id) {
		T result = executeInsideEntityManager(em -> em.find(entityClass, id));
		return Optional.ofNullable(result);
	}

	public void save(T entity) {
		executeInsideEntityManager(em -> {
			em.persist(entity);
			return null;
		});
	}

	public List<T> findAll() {
		return executeWithEntityManager(em -> {
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
