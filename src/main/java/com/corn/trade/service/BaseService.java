package com.corn.trade.service;

import com.corn.trade.jpa.IRepo;
import jakarta.persistence.EntityManager;

import java.util.List;

public abstract class BaseService {
	private final List<IRepo> repos = new java.util.ArrayList<>();

	protected void addRepo(IRepo repo) {
		repos.add(repo);
	}

	public void withEntityManager(EntityManager entityManager) {
		repos.forEach(repo -> repo.withEntityManager(entityManager));
	}

	public void closeEntityManager() {
		repos.forEach(IRepo::closeEntityManager);
	}
}
