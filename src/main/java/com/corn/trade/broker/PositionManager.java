package com.corn.trade.broker;

import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.OrderService;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.OrderType;
import com.corn.trade.util.ExchangeTime;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionManager {
	private static final Logger log = LoggerFactory.getLogger(PositionManager.class);

	private final TradeService tradeService;
	private final OrderService orderService;
	private final AssetService assetService;
	private final String       assetName;
	private final String       exchangeName;
	private       ExchangeTime exchangeTime;
	private boolean isPositionOpen = false;

	public PositionManager(Broker broker) throws BrokerException {
		tradeService = new TradeService();
		orderService = new OrderService();
		assetService = new AssetService();
		try {
			initExchangeTime(broker.getExchangeName());
		} catch (DBException e) {
			throw new BrokerException("DBException", e);
		}

		this.exchangeName = broker.getExchangeName();
		this.assetName = broker.getAssetName();
	}

	private void initExchangeTime(String exchangeName) throws DBException {
		Exchange exchange = assetService.getExchange(exchangeName);
		exchangeTime = new ExchangeTime(exchange);
	}


	public void openPosition(TradeData tradeData, OrderBracketIds bracketIds, OrderType mainOrderType) throws BrokerException {
		log.info("Opening position for {}", assetName);
		isPositionOpen = true;
		createDbRecords(tradeData, bracketIds, mainOrderType);
		// todo subscribe to trade context updates
	}

	private void createDbRecords(TradeData tradeData, OrderBracketIds bracketIds, OrderType mainOrderType) throws BrokerException {
		EntityManager entityManager = JpaUtil.getEntityManager();
		tradeService.withEntityManager(entityManager);
		orderService.withEntityManager(entityManager);
		try {
			entityManager.getTransaction().begin();
			Trade trade = tradeService.createTrade(assetName, exchangeName, tradeData);
			orderService.createBracketOrders(trade, tradeData, bracketIds, mainOrderType, exchangeTime);
			entityManager.getTransaction().commit();
		} catch (DBException e) {
			log.error("Error creating db records", e);
			entityManager.getTransaction().rollback();
			throw new BrokerException("Error creating db records", e);
		} finally {
			tradeService.closeEntityManager();
			orderService.closeEntityManager();
		}
	}
}
