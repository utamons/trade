package com.corn.trade.util;

import com.corn.trade.jpa.DBException;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderType;

@FunctionalInterface
public interface CreateOrderTrigger {
	Long createOrder(
			OrderRole role,
			OrderType type,
			double price,
			double auxPrice,
			double quantity,
			double limitPrice,
			long parentId) throws DBException;
}
