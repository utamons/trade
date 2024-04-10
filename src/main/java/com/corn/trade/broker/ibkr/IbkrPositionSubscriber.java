package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.Broker;
import com.corn.trade.model.Position;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * This is a subscriber for IBKR positions.
 * It also supports a list of listeners, which will be notified when a position is updated.
 */
public class IbkrPositionSubscriber {
	private final static Logger log = LoggerFactory.getLogger(IbkrPositionSubscriber.class);

	private final Map<String, Map<Integer, Consumer<Position>>> listeners  = new ConcurrentHashMap<>();
	private final Map<String, ApiController.IPnLSingleHandler>  handlers   = new HashMap<>();
	private final IbkrConnectionHandler                         connectionHandler;
	private       int                                           listenerId = 0;


	IbkrPositionSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	private static String getContractKey(int contractId, String account) {
		return contractId + "*" + account;
	}

	private void notifyListeners(int contractId, String account, Position position) {
		Map<Integer, Consumer<Position>> contractGroup = listeners.get(getContractKey(contractId, account));
		if (contractGroup != null) {
			contractGroup.values().forEach(listener -> listener.accept(position));
		}
	}

	public synchronized int addListener(int contractId, String account, Consumer<Position> listener) {
		Map<Integer, Consumer<Position>> contractGroup =
				this.listeners.computeIfAbsent(getContractKey(contractId, account), k -> new ConcurrentHashMap<>());
		contractGroup.put(++listenerId, listener);
		return listenerId;
	}

	public synchronized void removeListener(int contractId, String account, int listenerId) {
		Map<Integer, Consumer<Position>> contractGroup = this.listeners.get(getContractKey(contractId, account));
		if (contractGroup == null) {
			return;
		}

		contractGroup.remove(listenerId);

		if (contractGroup.isEmpty()) {
			listeners.remove(getContractKey(contractId, account));
			log.warn("No more listeners for contractId: {}, account: {}", contractId, account);
		}
	}

	public synchronized void subscribe(String symbol, int contractId, String account) {
		if (handlers.containsKey(getContractKey(contractId, account))) {
			log.info("Already subscribed to PnL for contractId: {}, account: {}", contractId, account);
			return;
		}
		log.info("Subscribing to PnL for contractId: {}, account: {}", contractId, account);
		ApiController.IPnLSingleHandler handler = (reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value) -> {
			Position position = Position.aPosition()
			                            .withQuantity(pos.longValue())
			                            .withUnrealizedPnl(unrealizedPnL)
			                            .withRealizedPnl(realizedPnL)
			                            .withMarketValue(value)
			                            .withSymbol(symbol)
			                            .build();
			notifyListeners(contractId, account, position);
		};

		ApiController.IPnLSingleHandler oldHandler = handlers.get(getContractKey(contractId, account));

		if (oldHandler != null) {
			connectionHandler.controller().cancelPnLSingle(oldHandler);
		}

		handlers.put(getContractKey(contractId, account), handler);

		connectionHandler.controller().reqPnLSingle(account, "", contractId, handler);
	}
}
