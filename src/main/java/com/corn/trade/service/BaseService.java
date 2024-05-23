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
			withEntityManager(JpaUtil.getEntityManager());
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

	protected void rollbackTransaction() {
		if (localEm) {
			entityManager.getTransaction().rollback();
			closeEntityManager();
			localEm = false;
		}
	}
}
