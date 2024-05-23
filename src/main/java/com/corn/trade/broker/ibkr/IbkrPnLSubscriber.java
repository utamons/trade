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
