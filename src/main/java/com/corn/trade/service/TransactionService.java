package com.corn.trade.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class TransactionService {
	private final   EntityManagerFactory entityManagerFactory;
	protected final EntityManager        entityManager;

	public TransactionService() {
		this.entityManagerFactory = null;
		this.entityManager = null;
	}

	public void createTransaction() {
	}
}
