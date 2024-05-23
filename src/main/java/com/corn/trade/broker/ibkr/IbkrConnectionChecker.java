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
package com.corn.trade.broker.ibkr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IbkrConnectionChecker {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IbkrConnectionChecker.class);

	CompletableFuture<Boolean> checkConnection(int attempts, IbkrConnectionHandler ibkrConnectionHandler) {
		log.debug("Testing connection, {} attempts remaining..", attempts);
		// Simulate the creation of adapter and checking connection

		return CompletableFuture.supplyAsync(() -> {
			// Simulate checking connection
			if (ibkrConnectionHandler.isConnected()) {
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
					return checkConnection(attempts - 1, ibkrConnectionHandler).join();
				} else {
					return false;
				}
			}
		});
	}
}
