package com.corn.trade.service;

import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.OrderRepo;
import com.corn.trade.model.ExecutionData;
import com.corn.trade.type.OrderRole;

import java.util.Objects;

import static com.corn.trade.util.Util.toBigDecimal;

public class OrderService extends BaseService {
	private final OrderRepo orderRepo;

	public OrderService() {
		orderRepo = new OrderRepo(Order.class);
	}

	public void saveExecution(Trade trade, OrderBracketIds bracketIds, ExecutionData executionData) {
		beginTransaction();
		try {
			Order order = new Order();
			order.setTrade(trade);
			order.setOrderId(executionData.orderId());
			if (Objects.equals(executionData.orderId(), bracketIds.mainId())) {
				order.setRole(OrderRole.MAIN.name());
			} else if (Objects.equals(executionData.orderId(), bracketIds.stopLossId())) {
				order.setRole(OrderRole.STOP_LOSS.name());
			} else if (Objects.equals(executionData.orderId(), bracketIds.takeProfitId())) {
				order.setRole(OrderRole.TAKE_PROFIT.name());
			} else {
				order.setRole(OrderRole.PARTIAL_CLOJURE.name());
			}
			order.setType(executionData.side());
			order.setQuantity(executionData.quantity());
			order.setAuxPrice(toBigDecimal(executionData.price()));
			order.setAvgPrice(toBigDecimal(executionData.avgPrice()));
			order.setExecutedAt(executionData.time());
			orderRepo.save(order);
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}
}
