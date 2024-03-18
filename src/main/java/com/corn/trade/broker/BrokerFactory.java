package com.corn.trade.broker;

import com.corn.trade.broker.ibkr.IbkrBroker;
import com.corn.trade.broker.ibkr.IbkrException;
import com.corn.trade.entity.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class BrokerFactory {
	private static final Logger                  log     = LoggerFactory.getLogger(BrokerFactory.class);
	private static final HashMap<String, Broker> brokers = new HashMap<>();

	public static Broker getBroker(String brokerName,
	                               String assetName,
	                               String exchangeName) throws BrokerException {
		String key = getKey(brokerName, assetName, exchangeName);
		if (brokerName.equals("IBKR")) {
			if (brokers.containsKey(key)) {
				log.debug("Broker " + brokerName + " found in cache.");
				return brokers.get(key);
			} else {
				log.debug("Broker " + brokerName + " not found in cache. Creating new one.");
				IbkrBroker broker;
				try {
					broker = new IbkrBroker(assetName, exchangeName);
				} catch (IbkrException e) {
					throw new BrokerException(e.getMessage());
				}
				log.debug("Broker " + brokerName + " created.");
				brokers.putIfAbsent(getKey(brokerName,assetName,broker.getExchangeName()), broker);
				return broker;
			}
		} else {
			throw new BrokerException("Broker " + brokerName + " not supported.");
		}
	}

	public static String getKey(String brokerName, String assetName, String exchangeName) {
		return brokerName+" "+assetName + " " + exchangeName;
	}
}
