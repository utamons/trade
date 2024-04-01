package com.corn.trade.service;

import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.OrderRepo;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderType;
import com.corn.trade.util.ExchangeTime;

import static com.corn.trade.util.Util.toBigDecimal;

public class OrderService extends BaseService {
	private final OrderRepo orderRepo;

	public OrderService() {
		orderRepo = new OrderRepo(Order.class);
	}

	public void createBracketOrders(Trade trade, TradeData tradeData, OrderBracketIds bracketIds, OrderType mainOrderType,
	                                ExchangeTime exchangeTime) {
		beginTransaction();

		try {
			Order order = new Order();
			order.setTrade(trade);
			order.setOrderId(String.valueOf(bracketIds.mainId()));
			order.setRole(OrderRole.MAIN.name());
			order.setType(mainOrderType.name());
			order.setQuantity(tradeData.getQuantity());
			order.setPrice(toBigDecimal(tradeData.getOrderLimit()));
			order.setAuxPrice(toBigDecimal(tradeData.getOrderStop()));
			order.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			order.setStatus("NEW");

			orderRepo.save(order);

			Order stopLossOrder = new Order();
			stopLossOrder.setTrade(trade);
            stopLossOrder.setOrderId(String.valueOf(bracketIds.stopLossId()));
			order.setRole(OrderRole.STOP_LOSS.name());
			stopLossOrder.setType(OrderType.STP.name());
			order.setQuantity(tradeData.getQuantity());
			stopLossOrder.setPrice(toBigDecimal(tradeData.getStopLoss()));
			stopLossOrder.setQuantity(tradeData.getQuantity());
			stopLossOrder.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			order.setStatus("NEW");

			orderRepo.save(stopLossOrder);

			Order takeProfitOrder = new Order();
			takeProfitOrder.setTrade(trade);
			takeProfitOrder.setOrderId(String.valueOf(bracketIds.takeProfitId()));
			order.setRole(OrderRole.TAKE_PROFIT.name());
			takeProfitOrder.setType(OrderType.LMT.name());
			order.setQuantity(tradeData.getQuantity());
			takeProfitOrder.setPrice(toBigDecimal(tradeData.getGoal()));
			takeProfitOrder.setQuantity(tradeData.getQuantity());
			takeProfitOrder.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			order.setStatus("NEW");

			orderRepo.save(takeProfitOrder);

			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}
}
