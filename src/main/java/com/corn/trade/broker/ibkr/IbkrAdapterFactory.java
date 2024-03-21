package com.corn.trade.broker.ibkr;

import java.util.concurrent.ExecutionException;

public class IbkrAdapterFactory {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IbkrAdapterFactory.class);
	private static IbkrAdapter adapter;
	public static synchronized IbkrAdapter getAdapter() {
		if (adapter == null) {
			adapter = new IbkrAdapter();
			adapter.setDisconnectionListener(() -> adapter = null);
			adapter.run();
		}
		log.info("testing connection");
		try {
			if (!new IbkrConnectionChecker().checkConnection(3,adapter).get()) {
				adapter = null;
				throw new IbkrException("Not connected to IBKR.");
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new IbkrException(e);
		}
		log.info("Connected to IBKR.");
		return adapter;
	}
}
