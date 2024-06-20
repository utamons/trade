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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import com.corn.trade.type.Stage;
import org.slf4j.LoggerFactory;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.INFO;

public class LoggerSetup {

	public static void configureLogging(Stage stage) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();

		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
		rollingFileAppender.setContext(loggerContext);
		rollingFileAppender.setName("ROLLING");

		// Set file path based on the environment
		String logFilePath = stage == Stage.PROD ? "E://log//prod_trade.log" : "E://log//dev_trade.log";
		rollingFileAppender.setFile(logFilePath);

		PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
		fileEncoder.setContext(loggerContext);
		fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36}: %msg%n");
		fileEncoder.start();

		rollingFileAppender.setEncoder(fileEncoder);

		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(loggerContext);
		rollingPolicy.setParent(rollingFileAppender);
		rollingPolicy.setFileNamePattern("E://log//" + (stage == Stage.PROD ? "prod_" : "dev_") + "trade.%d.log");
		rollingPolicy.setMaxHistory(7);
		rollingPolicy.start();

		rollingFileAppender.setRollingPolicy(rollingPolicy);
		rollingFileAppender.start();

		// Attach the appender to the trade logger
		Logger tradeLogger = (Logger) LoggerFactory.getLogger("com.corn.trade");
		tradeLogger.addAppender(rollingFileAppender);
		tradeLogger.setLevel(DEBUG);
		tradeLogger.setAdditive(false);  // If you want to disable the propagation to higher level loggers

		// Attach the appender to the root logger
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(rollingFileAppender);

		// Attach the appender to the mariadb logger
		Logger mariadbLogger = (Logger) LoggerFactory.getLogger("org.mariadb");
		mariadbLogger.addAppender(rollingFileAppender);
		mariadbLogger.setLevel(INFO);
		mariadbLogger.setAdditive(false);

		// Attach the appender to the hibernate logger
		Logger hibernateLogger = (Logger) LoggerFactory.getLogger("org.hibernate");
		hibernateLogger.addAppender(rollingFileAppender);
		hibernateLogger.setLevel(INFO);
		hibernateLogger.setAdditive(false);

		// Console appender with a separate encoder
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(loggerContext);

		PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
		consoleEncoder.setContext(loggerContext);
		consoleEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{36}: %msg%n");
		consoleEncoder.start();

		consoleAppender.setEncoder(consoleEncoder);
		consoleAppender.setName("CONSOLE");
		consoleAppender.start();

		// Attach the console appender to the trade logger
		tradeLogger.addAppender(consoleAppender);

		// Attach the console appender to the root logger
		rootLogger.addAppender(consoleAppender);

		// Attach the console appender to the mariadb logger
		mariadbLogger.addAppender(consoleAppender);

		// Optionally print the internal state
		StatusPrinter.print(loggerContext);
	}
}
