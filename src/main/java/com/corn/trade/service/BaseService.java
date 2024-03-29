package com.corn.trade.service;

import com.corn.trade.jpa.IRepo;
import com.corn.trade.jpa.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/*
  The class is not intended to be shared across threads, it is not thread-safe!
 */
public abstract class BaseService {
	private final List<IRepo> repos = new java.util.ArrayList<>();

	protected EntityManager entityManager;

	private boolean localEm = false;

	protected void addRepo(IRepo repo) {
		repos.add(repo);
	}

	public void withEntityManager(EntityManager entityManager) {
		if (this.entityManager != null) {
			throw new IllegalStateException("EntityManager already set");
		}
		this.entityManager = entityManager;
		repos.forEach(repo -> repo.withEntityManager(entityManager));
	}

	public void closeEntityManager() {
		repos.forEach(IRepo::closeEntityManager);
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
		entityManager = null;
	}

	protected void beginTransaction() {
		if (entityManager == null) {
			entityManager = JpaUtil.getEntityManager();
			withEntityManager(entityManager);
			localEm = true;
			entityManager.getTransaction().begin();
		}
	}

	protected void commitTransaction() {
		if (localEm) {
			entityManager.getTransaction().commit();
			closeEntityManager();
			localEm = false;
		}
	}
}
