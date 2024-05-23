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
package com.corn.trade.util;

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
	public static final Logger log = LoggerFactory.getLogger(LiquibaseRunner.class);

	public static void runLiquibase(String url, String username, String password) {
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
			Util.showErrorDlg(null,"Error running Liquibase - "+e.getMessage(), true);
			log.error("Error running Liquibase", e);
			System.exit(1);
		}
	}
}

