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

import com.corn.trade.model.Position;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * This is a subscriber for IBKR positions.
 * It also supports a list of listeners, which will be notified when a position is updated.
 */
public class IbkrPositionSubscriber {
	private final static Logger log = LoggerFactory.getLogger(IbkrPositionSubscriber.class);

	private final Map<String, Map<Integer, Consumer<Position>>> listeners = new ConcurrentHashMap<>();
	private final Map<String, ApiController.IPnLSingleHandler> handlers = new HashMap<>();
	private final IbkrConnectionHandler connectionHandler;
	private final Map<String, Boolean> initialState = new ConcurrentHashMap<>();
	private int listenerId = 0;

	IbkrPositionSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	private static String getContractKey(int contractId, String account) {
		return contractId + "*" + account;
	}

	private void notifyListeners(int contractId, String account, Position position) {
		Map<Integer, Consumer<Position>> contractGroup = listeners.get(getContractKey(contractId, account));
		if (contractGroup != null) {
			// Listeners can remove themselves from the list on accept(), so we need to make a copy
			Set<Map.Entry<Integer, Consumer<Position>>> listeners = Set.copyOf(contractGroup.entrySet());
			listeners.forEach(listener -> {
				// Include the listener id in the position for debugging purposes
				Position positionCopy = position.copy().withListenerId(listener.getKey()).build();
				listener.getValue().accept(positionCopy);
			});
		}
	}

	public synchronized int addListener(int contractId, String account, Consumer<Position> listener) {
		Map<Integer, Consumer<Position>> contractGroup =
				this.listeners.computeIfAbsent(getContractKey(contractId, account), k -> new ConcurrentHashMap<>());
		contractGroup.put(++listenerId, listener);
		log.debug("Added listener with id: {} for contractId: {}, account: {}", listenerId, contractId, account);
		return listenerId;
	}

	public synchronized void removeListener(int contractId, String account, int listenerId) {
		log.debug("Removing listener with id: {} for contractId: {}, account: {}", listenerId, contractId, account);
		Map<Integer, Consumer<Position>> contractGroup = listeners.get(getContractKey(contractId, account));
		if (contractGroup == null) {
			log.warn("Cannot remove listener with id: {} No listeners group found for contractId: {}, account: {}", listenerId, contractId, account);
			return;
		}

		Consumer<Position> listener = contractGroup.remove(listenerId);
		if (listener == null) {
			log.warn("Cannot remove listener with id: {} No listener found in the group for contractId: {}, account: {}", listenerId, contractId, account);
			return;
		}

		if (contractGroup.isEmpty()) {
			listeners.remove(getContractKey(contractId, account));
			log.warn("No more listeners for contractId: {}, account: {}", contractId, account);
		} else {
			log.debug("Remaining listeners for contractId: {}, account: {}", contractId, account);
			contractGroup.forEach((key, value) -> log.debug("Listener id: {}", key));
		}
	}

	public void removeAllListeners(int contractId, String account) {
		Map<Integer, Consumer<Position>> contractGroup = listeners.remove(getContractKey(contractId, account));
		if (contractGroup != null) {
			log.debug("Removed all listeners for contractId: {}, account: {}", contractId, account);
		} else {
			log.debug("No listeners group found for contractId: {}, account: {}", contractId, account);
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

			String key = getContractKey(contractId, account);
			boolean wasInitiallyEmpty = initialState.getOrDefault(key, true);

			// Check if the initial state is empty and skip notification if it is initially empty
			if (wasInitiallyEmpty && pos.longValue() == 0) {
				log.info("Skipping initial empty position notification for contractId: {}, account: {}", contractId, account);
				return;
			}

			// Update the initial state
			initialState.put(key, pos.longValue() == 0);

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
