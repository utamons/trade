package com.corn.trade.util;

import com.corn.trade.App;
import org.slf4j.Logger;

public class Debug {
	private final Logger log;
	private final int level = App.DEBUG_LEVEL;
	public Debug(Logger log) {
		this.log = log;
	}

	public void info(String msg, Object... args) {
		log.info(msg, args);
	}

	public void debug(int lvl, String msg, Object... args) {
		if (level >= lvl) {
			log.debug(msg, args);
		}
	}

	public void error(String msg, Object... args) {
		log.error(msg, args);
	}
}
