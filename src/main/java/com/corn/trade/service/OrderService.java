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

/*
  The class is not intended to be shared across threads, it is not thread-safe!
 */
public class OrderService extends BaseService {

	private final OrderRepo orderRepo;

	public OrderService() {
		this.orderRepo = new OrderRepo(Order.class);
		addRepo(orderRepo);
	}

	public void updateOrder(long id, long orderId, OrderStatus status, long filled, long remaining, double avgFillPrice) throws DBException {
		beginTransaction();
		try {
			Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
			order.setOrderId(String.valueOf(orderId));
			order.setStatus(status.name());
			order.setFilled(filled);
			order.setRemaining(remaining);
			order.setAvgFillPrice(BigDecimal.valueOf(avgFillPrice));
			commitTransaction();
		} catch (DBException e) {
			rollbackTransaction();
			throw e;
		}
	}

	public Order createOrder(Trade trade, TradeData tradeData, OrderRole orderRole, OrderType orderType, Order parentOrder) {
		beginTransaction();
		try {
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
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}

	public void updateOrderId(Long id, int orderId) throws DBException {
		beginTransaction();
		try {
			Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
			order.setOrderId(String.valueOf(orderId));
			commitTransaction();
		} catch (DBException e) {
			rollbackTransaction();
			throw e;
		}
	}

	public void updateOrderError(long id, String errorCode, String errorMsg) throws DBException {
		beginTransaction();
		try {
			Order order = orderRepo.findById(id).orElseThrow(() -> new DBException("Order not found"));
			order.setErrorCode(errorCode);
			order.setErrorMsg(errorMsg);
			commitTransaction();
		} catch (DBException e) {
			rollbackTransaction();
			throw e;
		}
	}
}
