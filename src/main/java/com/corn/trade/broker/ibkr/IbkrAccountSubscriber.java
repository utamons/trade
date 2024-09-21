package com.corn.trade.broker.ibkr;

import com.corn.trade.model.AccountUpdate;
import com.ib.controller.ApiController;
import com.ib.controller.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class IbkrAccountSubscriber {
	private final static Logger log = LoggerFactory.getLogger(IbkrAccountSubscriber.class);

	private final Map<String, Map<Integer, Consumer<AccountUpdate>>> listeners = new ConcurrentHashMap<>();
	private final Map<String, ApiController.IAccountHandler>         handlers  = new ConcurrentHashMap<>();

	private final IbkrConnectionHandler connectionHandler;
	private int listenerId = 0;

	IbkrAccountSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	private void notifyListeners(String account, AccountUpdate accountUpdate) {
		Map<Integer, Consumer<AccountUpdate>> contractGroup = listeners.get(account);
		if (contractGroup != null) {
			contractGroup.values().forEach(listener -> listener.accept(accountUpdate));
		}
	}

	public synchronized int addListener(String account, Consumer<AccountUpdate> listener) {
		log.info("Adding listener for account: {}", account);
		Map<Integer, Consumer<AccountUpdate>> contractGroup =
				this.listeners.computeIfAbsent(account, k -> new ConcurrentHashMap<>());
		contractGroup.put(++listenerId, listener);
		return listenerId;
	}

	public synchronized void subscribe(String account) {
		if (handlers.containsKey(account)) {
			log.info("Already subscribed to account updates for account: {}", account);
			return;
		}
		log.info("Subscribing to account updates for account: {}", account);
		ApiController.IAccountHandler handler = new ApiController.IAccountHandler() {

			@Override
			public void accountValue(String accountName, String key, String value, String currency) {
				log.info("Account value: {} {} {} {}", key, value, currency, accountName);
				if (key.equals("AvailableFunds")) {
					AccountUpdate accountUpdate = new AccountUpdate(accountName, key, value, currency);
					notifyListeners(accountName, accountUpdate);
				}
			}

			@Override
			public void accountTime(String timeStamp) {
				log.info("Account time: {}", timeStamp);
			}

			@Override
			public void accountDownloadEnd(String account) {
				log.info("Account download end: {}", account);
			}

			@Override
			public void updatePortfolio(Position position) {
				log.info("Portfolio update: {}", position);
			}
		};
		connectionHandler.controller().reqAccountUpdates(true, account, handler);
		handlers.put(account, handler);
	}
}
