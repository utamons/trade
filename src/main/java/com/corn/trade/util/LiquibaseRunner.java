package com.corn.trade.util;

import com.corn.trade.Trade;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class LiquibaseRunner {
	public static Logger log = LoggerFactory.getLogger(LiquibaseRunner.class);

	public static void runLiquibase() {
		// Database connection details
		String url           = Trade.DB_URL;
		String username      = Trade.DB_USER;
		String password      = Trade.DB_PASSWORD;
		String changeLogFile = "db/changelog/master.xml";

		try {
			DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
			Connection connection = DriverManager.getConnection(url, username, password);
			Database   database   =
					DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

			CommandScope updateCommandScope = new CommandScope("update");
			updateCommandScope.addArgumentValue("url", url);
			updateCommandScope.addArgumentValue("username", username);
			updateCommandScope.addArgumentValue("password", password);
			updateCommandScope.addArgumentValue("changeLogFile", changeLogFile);
			updateCommandScope.addArgumentValue("database", database);
			updateCommandScope.addArgumentValue("resourceAccessor", new ClassLoaderResourceAccessor());

			updateCommandScope.execute();
		} catch (Exception e) {
			log.error("Error running Liquibase", e);
		}
	}
}

