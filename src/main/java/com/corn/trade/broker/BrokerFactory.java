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
		final String key = getKey(brokerName, assetName, exchangeName);
		if (brokerName.equals("IBKR")) {
			if (brokers.containsKey(key)) {
				log.debug("Broker " + key + " found in cache.");
				return brokers.get(key);
			} else {
				log.debug("Broker " + key + " not found in cache. Trying to create new one.");
				IbkrBroker broker;
				try {
					broker = new IbkrBroker(assetName, exchangeName,
					                        () -> {
						                        brokers.remove(key);
						                        disconnectionTrigger.trigger();
					                        }
					);
				} catch (IbkrException e) {
					throw new BrokerException(e.getMessage());
				}
				if (!exchangeName.equals(broker.getExchangeName())) {
					log.debug(assetName + " is found in " + broker.getExchangeName() + " instead of " + exchangeName + ".");
				}
				final String confirmedKey = getKey(brokerName, assetName, broker.getExchangeName());
				if (brokers.containsKey(confirmedKey)) {
					log.debug("Broker " + key + " found in cache.");
					return brokers.get(confirmedKey);
				} else {
					log.debug("Broker " + confirmedKey + " created.");
					brokers.put(confirmedKey, broker);
					return broker;
				}
			}
		} else {
			throw new BrokerException("Broker " + brokerName + " not supported.");
		}
	}

	public static String getKey(String brokerName, String assetName, String exchangeName) {
		return brokerName+"/"+assetName + "/" + exchangeName;
	}
}
