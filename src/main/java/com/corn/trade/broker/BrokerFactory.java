package com.corn.trade.broker;

import com.corn.trade.broker.ibkr.IbkrBroker;
import com.corn.trade.broker.ibkr.IbkrException;
import com.corn.trade.util.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class BrokerFactory {
	private static final Logger                  log     = LoggerFactory.getLogger(BrokerFactory.class);
	private static final HashMap<String, Broker> brokers = new HashMap<>();

	public static Broker getBroker(String brokerName,
	                               String assetName,
	                               String exchangeName,
	                               Trigger disconnectionTrigger) throws BrokerException {
		final String initialKey = createKey(brokerName, assetName, exchangeName);
		Broker       broker     = brokers.get(initialKey);

		if (broker != null) {
			log.debug("Broker {} found in cache.", initialKey);
			return broker;
		}

		broker = instantiateBroker(brokerName, assetName, exchangeName, disconnectionTrigger);

		/*
		  Some brokers may provide assets from different exchanges, but the exchange name
		  may be different from the one provided by the user. In this case, we need to adjust
		  the exchange name to the one provided by the broker.
		 */
		String actualExchangeName = broker.getExchangeName();
		String actualKey          = createKey(brokerName, assetName, actualExchangeName);

		if (!initialKey.equals(actualKey) && brokers.containsKey(actualKey)) {
			log.debug("Broker {} already created after exchange name adjustment.", actualKey);
			return brokers.get(actualKey);
		}

		log.debug("Caching broker {}.", actualKey);
		brokers.put(actualKey, broker);
		return broker;
	}

	public static Broker instantiateBroker(String brokerName,
	                                        String assetName,
	                                        String exchangeName,
	                                        Trigger disconnectionTrigger) throws BrokerException {
		String key = createKey(brokerName, assetName, exchangeName);
		//noinspection SwitchStatementWithTooFewBranches
		switch (brokerName) {
			case "IBKR":
				try {
					IbkrBroker broker = new IbkrBroker(assetName, exchangeName, () -> {
						brokers.remove(key);
						disconnectionTrigger.trigger();
					});
					broker.setName(key);
					broker.setAssetName(assetName);
					log.debug("Created new IBKR broker for {}, exchange {}", assetName, exchangeName);
					return broker;
				} catch (IbkrException e) {
					throw new BrokerException("Failed to instantiate IBKR Broker: " + e.getMessage(), e);
				}
				// Future case for new brokers here
			default:
				throw new BrokerException("Unsupported broker: " + brokerName);
		}
	}

	public static String createKey(String brokerName, String assetName, String exchangeName) {
		return brokerName + "/" + assetName + "/" + exchangeName;
	}
}
