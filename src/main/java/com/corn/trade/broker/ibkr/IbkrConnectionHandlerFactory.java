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

import java.util.concurrent.ExecutionException;

public class IbkrConnectionHandlerFactory {
	private static final org.slf4j.Logger      log = org.slf4j.LoggerFactory.getLogger(IbkrConnectionHandlerFactory.class);
	private static       IbkrConnectionHandler connectionHandler;
	static synchronized IbkrConnectionHandler getConnectionHandler() {
		if (connectionHandler == null) {
			connectionHandler = new IbkrConnectionHandler();
			connectionHandler.setDisconnectionListener(() -> connectionHandler = null);
			connectionHandler.run();
		}
		log.info("testing connection");
		try {
			if (!new IbkrConnectionChecker().checkConnection(3, connectionHandler).get()) {
				connectionHandler = null;
				throw new IbkrException("Not connected to IBKR.");
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new IbkrException(e);
		}
		log.info("Connected to IBKR.");
		return connectionHandler;
	}
}
