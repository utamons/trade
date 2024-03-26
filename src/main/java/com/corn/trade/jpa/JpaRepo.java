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
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JpaRepo.class);
	private final Class<T>             type;
	private final EntityManagerFactory entityManagerFactory;
	protected final EntityManager        entityManager;

	public JpaRepo(Class<T> type) {
		this.type = type;

		String url      = TradeWindow.DB_URL;
		String username = TradeWindow.DB_USER;
		String password = TradeWindow.DB_PASSWORD;

		Map<String, String> properties = new HashMap<>();
		properties.put("jakarta.persistence.jdbc.user", username);
		properties.put("jakarta.persistence.jdbc.password", password);
		properties.put("jakarta.persistence.jdbc.url", url);

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
			log.error("Error saving an entity: {} : {}", e, e.getMessage());
			entityManager.getTransaction().rollback();
			throw e;
		}
	}

	public List<T> findAll() {
		try {
			TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + type.getSimpleName() + " e", type);
			return query.getResultList();
		} catch (RuntimeException e) {
			log.error("Error finding all {} : {}", e, e.getMessage());
			throw new RuntimeException(e);
		}
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
