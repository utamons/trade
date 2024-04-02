package com.corn.trade.broker;

import com.corn.trade.BaseWindow;
import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.model.BracketOrders;
import com.corn.trade.model.ExecutionData;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.AssetService;
import com.corn.trade.service.OrderService;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.TradeStatus;
import com.corn.trade.util.ExchangeTime;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PositionManager {
	private static final Logger log = LoggerFactory.getLogger(PositionManager.class);

	private final double priceThreshold = BaseWindow.ORDER_LUFT * 4;

	private final TradeService tradeService;
	private final OrderService orderService;
	private final AssetService assetService;
	private final String       assetName;
	private final String       exchangeName;
	private final Broker       broker;
	public        TradeStatus  tradeStatus;
	private       ExchangeTime exchangeTime;
	private       int          tradeContextId = 0;
	private       Double       currentPrice;
	private       Timer        updateStateTimer;

	private BracketOrders bracketOrders;

	private Trade trade;

	private CompletableFuture<List<ExecutionData>> executionDataFuture;

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
		this.broker = broker;
		updateStateTimer = new Timer(1000, e -> {
			updateState();
		});
		updateStateTimer.setRepeats(true);
		checkAndRestoreState();
	}

	// initial check if we have a broken session and need to restore state
	private void checkAndRestoreState() {

	}

	// If we have a not closed trade
	private void updateState() {
		// We should check all orders
		// 1. Get all open orders and update statuses
		// 2. Request executions for open orders if price is close to order


		boolean shouldRequestExecution = isPriceCloseToOrder(bracketOrders.takeProfitOrder());
	}

	private boolean isPriceCloseToOrder(Order order) {
		return order.getStatus().equals(OrderStatus.NEW.name()) && Math.abs(currentPrice - order.getPrice().doubleValue()) < priceThreshold;
	}

	private void initExchangeTime(String exchangeName) throws DBException {
		Exchange exchange = assetService.getExchange(exchangeName);
		exchangeTime = new ExchangeTime(exchange);
	}


	public void openPosition(TradeData tradeData,
	                         OrderBracketIds bracketIds,
	                         OrderType mainOrderType) throws BrokerException {
		log.info("Opening position for {}", assetName);
		if (tradeStatus != null) {
			throw new BrokerException("Position already exists");
		}
		createDbRecords(tradeData, bracketIds, mainOrderType);
		tradeStatus = TradeStatus.NEW;
		tradeContextId = broker.requestTradeContext((tradeContext) -> {
			currentPrice = tradeContext.getPrice();
		});
		updateStateTimer.start();
	}

	private void closePosition() {
		log.info("Closing position for {}", assetName);
		tradeStatus = TradeStatus.CLOSED;
		broker.cancelTradeContext(tradeContextId);
	}

	private void createDbRecords(TradeData tradeData,
	                             OrderBracketIds bracketIds,
	                             OrderType mainOrderType) throws BrokerException {
		EntityManager entityManager = JpaUtil.getEntityManager();
		tradeService.withEntityManager(entityManager);
		orderService.withEntityManager(entityManager);
		try {
			entityManager.getTransaction().begin();
			trade = tradeService.saveNewTrade(assetName, exchangeName, tradeData);
			bracketOrders = orderService.saveNewBracketOrders(trade, tradeData, bracketIds, mainOrderType, exchangeTime);
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

	public boolean isPositionOpen() {
		return isPositionOpen;
	}
}
