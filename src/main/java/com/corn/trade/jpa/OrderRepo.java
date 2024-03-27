package com.corn.trade.jpa;

import com.corn.trade.entity.Order;

public class OrderRepo extends JpaRepo<Long, Order> {
	public OrderRepo(Class<Long> type) {
		super(type);
	}
}
