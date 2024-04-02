package com.corn.trade.service;

import com.corn.trade.broker.OrderBracketIds;
import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.OrderRepo;
import com.corn.trade.model.BracketOrders;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.OrderRole;
import com.corn.trade.type.OrderStatus;
import com.corn.trade.type.OrderType;
import com.corn.trade.util.ExchangeTime;

import static com.corn.trade.util.Util.toBigDecimal;

public class OrderService extends BaseService {
	private final OrderRepo orderRepo;

	public OrderService() {
		orderRepo = new OrderRepo(Order.class);
	}

	public BracketOrders saveNewBracketOrders(Trade trade, TradeData tradeData, OrderBracketIds bracketIds, OrderType mainOrderType,
	                                          ExchangeTime exchangeTime) {
		beginTransaction();

		Order mainOrder, stopLossOrder, takeProfitOrder;

		try {
			mainOrder = new Order();
			mainOrder.setTrade(trade);
			mainOrder.setOrderId(String.valueOf(bracketIds.mainId()));
			mainOrder.setRole(OrderRole.MAIN.name());
			mainOrder.setType(mainOrderType.name());
			mainOrder.setQuantity(tradeData.getQuantity());
			mainOrder.setPrice(toBigDecimal(tradeData.getOrderLimit()));
			mainOrder.setAuxPrice(toBigDecimal(tradeData.getOrderStop()));
			mainOrder.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			mainOrder.setStatus(OrderStatus.NEW.name());

			orderRepo.save(mainOrder);

			stopLossOrder = new Order();
			stopLossOrder.setTrade(trade);
            stopLossOrder.setOrderId(String.valueOf(bracketIds.stopLossId()));
			stopLossOrder.setRole(OrderRole.STOP_LOSS.name());
			stopLossOrder.setType(OrderType.STP.name());
			stopLossOrder.setQuantity(tradeData.getQuantity());
			stopLossOrder.setPrice(toBigDecimal(tradeData.getStopLoss()));
			stopLossOrder.setQuantity(tradeData.getQuantity());
			stopLossOrder.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			stopLossOrder.setStatus(OrderStatus.NEW.name());

			orderRepo.save(stopLossOrder);

			takeProfitOrder = new Order();
			takeProfitOrder.setTrade(trade);
			takeProfitOrder.setOrderId(String.valueOf(bracketIds.takeProfitId()));
			takeProfitOrder.setRole(OrderRole.TAKE_PROFIT.name());
			takeProfitOrder.setType(OrderType.LMT.name());
			takeProfitOrder.setQuantity(tradeData.getQuantity());
			takeProfitOrder.setPrice(toBigDecimal(tradeData.getGoal()));
			takeProfitOrder.setQuantity(tradeData.getQuantity());
			takeProfitOrder.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			takeProfitOrder.setStatus(OrderStatus.NEW.name());

			orderRepo.save(takeProfitOrder);

			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
		return new BracketOrders(mainOrder, stopLossOrder, takeProfitOrder);
	}
}
