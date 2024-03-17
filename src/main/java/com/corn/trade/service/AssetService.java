package com.corn.trade.service;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.broker.BrokerFactory;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.AssetRepo;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.ExchangeRepo;
import com.corn.trade.jpa.JpaRepo;

import java.util.List;
import java.util.Optional;

public class AssetService {
	private final ExchangeRepo exchangeRepo = new ExchangeRepo();
	private final AssetRepo    assetRepo    = new AssetRepo();

	public List<Exchange> getExchanges() {
		return exchangeRepo.findAll().stream().sorted().toList();
	}

	public List<Asset> getTickers() {
		return assetRepo.findAll().stream().sorted().toList();
	}

	public List<String> getTickerNames() {
		return getTickers().stream().map(Asset::getName).toList();
	}

	public List<String> getExchangeNames() {
		return getExchanges().stream().map(Exchange::getName).toList();
	}

	public Asset getAsset(String assetName, String exchangeName) throws DBException, BrokerException {
		Exchange exchange = exchangeRepo.findExchange(exchangeName).orElseThrow(() -> new DBException("Exchange " + exchangeName + " not found."));

		Optional<Asset> asset = assetRepo.findAsset(assetName, exchange);

		if (asset.isEmpty()) {
			Broker broker = BrokerFactory.getBroker("IBKR", assetName, exchangeName);
			String confirmedExchangeName = broker.getExchangeName();
			Optional<Exchange> confirmedExchange = exchangeRepo.findExchange(confirmedExchangeName);

			if (confirmedExchange.isEmpty()) {
				throw new BrokerException("Asset " + assetName + " belongs to " + exchangeName + " which is not supported.");
			}

			Asset newAsset = new Asset();
			newAsset.setName(assetName);
			newAsset.setExchange(confirmedExchange.get());
			assetRepo.save(newAsset);

			return newAsset;
		} else {
			return asset.get();
		}
	}
}
