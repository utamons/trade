package com.corn.trade.service;

import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.OrderRepo;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;

public class OrderService extends BaseService {

	private final OrderRepo orderRepo;

	public OrderService() {
		this.orderRepo = new OrderRepo(Long.class);
		addRepo(orderRepo);
	}


	public void updateOrder(long id, long orderId, OrderStatus status, long filled, long remaining, double avgFillPrice) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Order createOrder(Trade trade, TradeData tradeData, OrderRole orderRole) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void updateOrderIds(Long id, int orderId, int parentId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public void updateOrderError(long id, String errorCode, String errorMsg) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
