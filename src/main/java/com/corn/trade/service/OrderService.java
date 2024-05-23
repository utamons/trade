/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
