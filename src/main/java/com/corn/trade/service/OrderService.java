package com.corn.trade.service;

import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;
import com.corn.trade.type.PositionType;

public class OrderService {
	public Long createOrder(Trade trade,
	                        PositionType positionType,
	                        OrderRole role,
	                        OrderType type,
	                        double price,
	                        double auxPrice,
	                        double quantity,
	                        double limitPrice,
	                        long parentId) throws DBException {
		throw new UnsupportedOperationException("Not implemented yet");
		//return null;
	}

	public void updateOrder(long id, OrderStatus status, double filled, double remaining, double avgFillPrice) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
