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
