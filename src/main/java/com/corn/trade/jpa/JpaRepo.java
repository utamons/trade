package com.corn.trade.jpa;

import com.corn.trade.TradeWindow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class JpaRepo<T, ID extends Serializable> {

	private final Class<T>             type;
	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager        entityManager;

	public JpaRepo(Class<T> type) {
		this.type = type;

		String url           = TradeWindow.DB_URL;
		String username      = TradeWindow.DB_USER;
		String password      = TradeWindow.DB_PASSWORD;

		Map<String, String> properties = new HashMap<>();
		properties.put("javax.persistence.jdbc.user", username);
		properties.put("javax.persistence.jdbc.password", password);
		properties.put("javax.persistence.jdbc.url", url);

		this.entityManagerFactory = Persistence.createEntityManagerFactory("TradePersistenceUnit", properties);
		this.entityManager = entityManagerFactory.createEntityManager();
	}

	public Optional<T> findById(ID id) {
		T result = entityManager.find(type, id);
		return Optional.ofNullable(result);
	}

	public void save(T entity) {
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(entity);
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
