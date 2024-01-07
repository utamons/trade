package com.corn.trade.util;

import com.corn.trade.App;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Debug {
	private final Logger log;
	private final int level = App.DEBUG_LEVEL;
	public Debug(Logger log) {
		this.log = log;
	}

	@SuppressWarnings("unused")
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

	public static void main(String[] args) {
		String fileName = args[0];
		String search = "Placed main";

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(search)) {
					System.out.println(line);
				}
			}
		} catch (IOException e) {
			//noinspection CallToPrintStackTrace
			e.printStackTrace();
		}
	}
}
