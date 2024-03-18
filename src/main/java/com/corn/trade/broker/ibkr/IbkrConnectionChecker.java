package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.BrokerException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IbkrConnectionChecker {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IbkrConnectionChecker.class);

	CompletableFuture<Boolean> checkConnection(int attempts, IbkrAdapter ibkrAdapter) {
		log.debug("Testing connection, {} attempts remaining..", attempts);
		// Simulate the creation of adapter and checking connection

		return CompletableFuture.supplyAsync(() -> {
			// Simulate checking connection
			if (ibkrAdapter.isConnected()) {
				return true;
			} else {
				log.debug("Not connected to IBKR. Retrying...");
				if (attempts > 0) {
					try {
						// Wait for a bit before retrying
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException("Interrupted during sleep", e);
					}
					return checkConnection(attempts - 1, ibkrAdapter).join();
				} else {
					return false;
				}
			}
		});
	}
}
