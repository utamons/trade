package com.corn.trade.broker;

import com.corn.trade.broker.ibkr.IbkrBroker;
import com.corn.trade.entity.Exchange;

import java.util.HashMap;
import java.util.List;

public class BrokerFactory {
	private static final HashMap<String, Broker> brokers = new HashMap<>();

	public static Broker getBroker(String brokerName,
	                               String assetName,
	                               String exchangeName) throws BrokerException {
		final String key = getKey(brokerName, assetName, exchangeName);
		if (brokerName.equals("IBKR")) {
			if (brokers.containsKey(key)) {
				return brokers.get(key);
			} else {
				IbkrBroker broker = new IbkrBroker(assetName, exchangeName);
				brokers.put(key, broker);
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
