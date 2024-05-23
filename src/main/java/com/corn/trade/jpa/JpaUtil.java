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
package com.corn.trade.jpa;

import com.corn.trade.BaseWindow;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {
	private static final EntityManagerFactory emFactory;

	static {
		BaseWindow.loadProperties();
		String url      = BaseWindow.DB_URL;
		String username = BaseWindow.DB_USER;
		String password = BaseWindow.DB_PASSWORD;

		Map<String, String> properties = new HashMap<>();
		properties.put("jakarta.persistence.jdbc.user", username);
		properties.put("jakarta.persistence.jdbc.password", password);
		properties.put("jakarta.persistence.jdbc.url", url);

		emFactory = Persistence.createEntityManagerFactory("TradePersistenceUnit", properties);
	}

	public static EntityManager getEntityManager() {
		return emFactory.createEntityManager();
	}

	public static void close() {
		emFactory.close();
	}
}
