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
package com.corn.trade.broker;

import com.corn.trade.broker.ibkr.IbkrBroker;
import com.corn.trade.broker.ibkr.IbkrException;
import com.corn.trade.broker.simulation.SimulationBroker;
import com.corn.trade.util.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

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
		broker.setName(actualKey);
		brokers.put(actualKey, broker);
		return broker;
	}

	public static Optional<Broker> findBroker(String key) {
		return Optional.ofNullable(brokers.get(key));
	}

	public static Broker instantiateBroker(String brokerName,
	                                       String assetName,
	                                       String exchangeName,
	                                       Trigger disconnectionTrigger) throws BrokerException {
		String key = createKey(brokerName, assetName, exchangeName);
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
			case "TEST": {
				SimulationBroker broker = new SimulationBroker(disconnectionTrigger);
				broker.setName(key);
				broker.setAssetName(assetName);
				log.debug("Created new Test broker for {}, exchange {}", assetName, exchangeName);
				return broker;
			}
			default:
				throw new BrokerException("Unsupported broker: " + brokerName);
		}
	}

	public static String createKey(String brokerName, String assetName, String exchangeName) {
		return brokerName + "/" + assetName + "/" + exchangeName;
	}
}
