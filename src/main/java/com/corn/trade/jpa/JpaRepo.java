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
