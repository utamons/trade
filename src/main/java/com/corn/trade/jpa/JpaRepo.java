package com.corn.trade.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class JpaRepo<T, ID extends Serializable> {

	private final Class<T>             type;
	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager        entityManager;

	public JpaRepo(Class<T> type) {
		this.type = type;
		this.entityManagerFactory = Persistence.createEntityManagerFactory("TradePersistenceUnit");
		this.entityManager = entityManagerFactory.createEntityManager();
	}

	public Optional<T> findById(ID id) {
		T result = entityManager.find(type, id);
		return Optional.ofNullable(result);
	}

	public void save(T entity) {
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(entity); // Using merge
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			entityManager.getTransaction().rollback();
			throw e;
		}
	}

	public List<T> findAll() {
		TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type);
		return query.getResultList();
	}

	public void close() {
		if (entityManager != null) {
			entityManager.close();
		}
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}
	}
}
