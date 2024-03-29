package com.corn.trade.service;

import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.OrderRepo;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;

import java.math.BigDecimal;

public class OrderService extends BaseService {

	private final OrderRepo orderRepo;

	public OrderService() {
		this.orderRepo = new OrderRepo(Order.class);
		addRepo(orderRepo);
	}

	public synchronized void updateOrder(long id, long orderId, OrderStatus status, long filled, long remaining, double avgFillPrice) throws DBException {
		beginTransaction();
		Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
		order.setOrderId(String.valueOf(orderId));
		order.setStatus(status.name());
		order.setFilled(filled);
		order.setRemaining(remaining);
		order.setAvgFillPrice(BigDecimal.valueOf(avgFillPrice));
		commitTransaction();
	}

	public synchronized Order createOrder(Trade trade, TradeData tradeData, OrderRole orderRole, OrderType orderType, Order parentOrder) {
		beginTransaction();
		Order order = new Order();
		order.setTrade(trade);
		order.setAsset(trade.getAsset());
		order.setPositionType(tradeData.getPositionType().name());
		order.setRole(orderRole.name());
		order.setType(orderType.name());
		order.setPrice(BigDecimal.valueOf(tradeData.getPrice()));
		order.setQuantity(tradeData.getQuantity());
		order.setStatus(OrderStatus.NEW.name());
		if (parentOrder != null) {
			order.setParentOrder(parentOrder);
		}
		orderRepo.save(order);
		commitTransaction();
		return order;
	}

	public synchronized void updateOrderId(Long id, int orderId) throws DBException {
		beginTransaction();
		Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
		order.setOrderId(String.valueOf(orderId));
		commitTransaction();
	}

	public synchronized void updateOrderError(long id, String errorCode, String errorMsg) throws DBException {
		beginTransaction();
		Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
		order.setErrorCode(errorCode);
		order.setErrorMsg(errorMsg);
		commitTransaction();
	}
}
