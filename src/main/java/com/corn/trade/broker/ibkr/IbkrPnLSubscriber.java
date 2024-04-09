package com.corn.trade.broker.ibkr;

import com.corn.trade.model.PnL;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class IbkrPnLSubscriber {
	private final static Logger log = LoggerFactory.getLogger(IbkrPnLSubscriber.class);

	private final Map<String, Map<Integer, Consumer<PnL>>> listeners = new ConcurrentHashMap<>();
	private final Map<String, ApiController.IPnLHandler> handlers  = new ConcurrentHashMap<>();

	private final IbkrConnectionHandler                        connectionHandler;
	private int                                                listenerId = 0;

	IbkrPnLSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	private void notifyListeners(String account, PnL pnl) {
		Map<Integer, Consumer<PnL>> contractGroup = listeners.get(account);
		if (contractGroup != null) {
			contractGroup.values().forEach(listener -> listener.accept(pnl));
		}
	}

	public synchronized int addListener(String account, Consumer<PnL> listener) {
		log.info("Adding listener for account: {}", account);
		Map<Integer, Consumer<PnL>> contractGroup =
				this.listeners.computeIfAbsent(account, k -> new ConcurrentHashMap<>());
		contractGroup.put(++listenerId, listener);
		return listenerId;
	}

	public synchronized void removeListener(String account, int listenerId) {
		Map<Integer, Consumer<PnL>> contractGroup = this.listeners.get(account);
		if (contractGroup == null) {
			return;
		}

		contractGroup.remove(listenerId);

		if (contractGroup.isEmpty()) {
			listeners.remove(account);
			log.warn("No more listeners for account: {}", account);
		}
	}

	public synchronized void subscribe(String account) {
		if (handlers.containsKey(account)) {
			log.info("Already subscribed to PnL for account: {}", account);
			return;
		}
		log.info("Subscribing to PnL for account: {}", account);
		ApiController.IPnLHandler handler = (reqId, dailyPnL, unrealizedPnL, realizedPnL) -> {
			PnL pnl = new PnL(dailyPnL, realizedPnL, unrealizedPnL);
			notifyListeners(account, pnl);
		};
		connectionHandler.controller().reqPnL(account, "", handler);
		handlers.put(account, handler);
	}
}
