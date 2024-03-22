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
