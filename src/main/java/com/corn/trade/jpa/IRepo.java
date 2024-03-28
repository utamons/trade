package com.corn.trade.jpa;

import jakarta.persistence.EntityManager;

public interface IRepo {
	void withEntityManager(EntityManager entityManager);
	EntityManager getEntityManager();
	void closeEntityManager();
}
