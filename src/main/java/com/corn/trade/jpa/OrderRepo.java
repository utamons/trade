package com.corn.trade.jpa;

import com.corn.trade.entity.Order;

public class OrderRepo extends JpaRepo<Order, Long> {

	public OrderRepo(Class<Order> entityClass) {
		super(entityClass);
	}
}
