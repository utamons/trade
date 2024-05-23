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
package com.corn.trade.service;

import com.corn.trade.broker.Broker;
import com.corn.trade.broker.BrokerException;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.AssetRepo;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.ExchangeRepo;
import com.corn.trade.type.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.corn.trade.BaseWindow.STAGE;

/*
  The class is not intended to be shared across threads, it is not thread-safe!
 */
@SuppressWarnings("LoggingSimilarMessage")
public class AssetService extends BaseService {
	private static final Logger       log       = LoggerFactory.getLogger(AssetService.class);
	private final        ExchangeRepo exchangeRepo;
	private final        AssetRepo    assetRepo;

	public AssetService(ExchangeRepo exchangeRepo, AssetRepo assetRepo) {
		this.exchangeRepo = exchangeRepo;
		this.assetRepo = assetRepo;
		addRepo(exchangeRepo);
		addRepo(assetRepo);
	}

	public AssetService() {
		this.exchangeRepo = new ExchangeRepo();
		this.assetRepo = new AssetRepo();
		addRepo(exchangeRepo);
		addRepo(assetRepo);
	}

	public List<Exchange> getExchanges() {
		return exchangeRepo.findAll().stream().sorted().toList();
	}

	public List<Asset> getAssets() {
		return assetRepo.findAll().stream().sorted().toList();
	}

	public List<String> getAssetNames() {
		return getAssets().stream().map(Asset::getName).toList();
	}

	public List<String> getExchangeNames() {
		return getExchanges().stream()
		                     .map(Exchange::getName)
				             .filter(name -> STAGE != Stage.PROD || !name.equals("TEST"))
		                     .toList();
	}

	public Exchange getExchange(String exchangeName) throws DBException {
		return exchangeRepo.findExchange(exchangeName)
		                   .orElseThrow(() -> new DBException("Exchange " + exchangeName + " not found."));
	}

	public Asset getAsset(String assetName, String exchangeName, Broker broker) throws DBException, BrokerException {
		log.debug("start");
		beginTransaction();
		try {
			Exchange exchange = getExchange(exchangeName);

			Optional<Asset> asset = assetRepo.findAsset(assetName, exchange);

			if (asset.isEmpty()) {
				log.debug("Asset {}/{} not found. Trying to get it from broker.", assetName, exchangeName);
				String             confirmedExchangeName = broker.getExchangeName();
				Optional<Exchange> confirmedExchangeOpt  = exchangeRepo.findExchange(confirmedExchangeName);

				if (confirmedExchangeOpt.isEmpty()) {
					throw new BrokerException("Asset " +
					                          assetName +
					                          " belongs to " +
					                          exchangeName +
					                          " which is not supported" +
					                          ".");
				}

				Exchange confirmedExchange = confirmedExchangeOpt.get();

				log.debug("Asset {}/{} found in broker.", assetName, confirmedExchangeName);

				asset = assetRepo.findAsset(assetName, confirmedExchange);

				if (asset.isPresent()) {
					log.debug("Asset {}/{} found in database.", assetName, confirmedExchangeName);

					log.debug("finish");
					commitTransaction();
					return asset.get();
				}

				log.debug("Saving asset {}/{} to database.", assetName, confirmedExchangeName);

				Asset newAsset = new Asset();
				newAsset.setName(assetName);
				newAsset.setExchange(confirmedExchange);
				assetRepo.save(newAsset);
				commitTransaction();

				log.debug("finish");
				return newAsset;
			} else {
				commitTransaction();
				log.debug("finish");
				return asset.get();
			}
		} catch (DBException | BrokerException e) {
			rollbackTransaction();
			throw e;
		}
	}
}
