package com.corn.trade.broker.ibkr;

import com.corn.trade.TradeWindow;
import com.corn.trade.broker.OrderBracketIds;
import com.ib.client.*;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.round;

class IbkrOrderHelper {

	private static final Logger                log = org.slf4j.LoggerFactory.getLogger(IbkrOrderHelper.class);
	private final        IbkrConnectionHandler ibkrConnectionHandler;

	public IbkrOrderHelper(IbkrConnectionHandler ibkrConnectionHandler) {
		this.ibkrConnectionHandler = ibkrConnectionHandler;
	}

	public void placeOrder(ContractDetails contractDetails,
	                       long quantity,
	                       Double stop,
	                       Double limit,
	                       Action action,
	                       OrderType orderType,
	                       Consumer<Boolean> executionListener) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}

		Order main = prepareOrder(quantity, stop, limit, action, orderType, true);

		ibkrConnectionHandler.controller()
		                     .placeOrModifyOrder(contractDetails.contract(),
		                                         main,
		                                         new IbkrOrderHandler(contractDetails.contract(),
		                                                              main, executionListener));

		log.info("Placed order {} {} {}, LMT: {}, QTT: {}, STP: {}",
		         main.orderId(),
		         main.action(),
		         contractDetails.contract().symbol(),
		         round(limit),
		         quantity,
		         round(stop));
	}

	private static Order prepareOrder(long quantity, Double stop, Double limit, Action action, OrderType orderType, boolean transmit) {
		Order order = new Order();
		order.action(action);
		order.orderType(orderType);

		Decimal quantityDecimal = Decimal.get(quantity);
		order.totalQuantity(quantityDecimal);
		order.lmtPrice(round(limit));
		if (orderType == OrderType.STP || orderType == OrderType.STP_LMT) {
			order.auxPrice(round(stop));
		}
		order.transmit(transmit);
		return order;
	}

	public void setStopLossQuantity(int orderId, Contract contract, long quantity, double stopLossPrice, Action action) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}

		Order stopLoss = new Order();
		stopLoss.orderId(orderId);
		stopLoss.action(action);
		stopLoss.orderType(OrderType.STP);
		stopLoss.auxPrice(round(stopLossPrice));
		stopLoss.totalQuantity(Decimal.get(quantity));
		stopLoss.transmit(true);

		ibkrConnectionHandler.controller()
		                     .placeOrModifyOrder(contract,
		                                         stopLoss,
		                                         new IbkrOrderHandler(contract, stopLoss));

		log.info("Decreased SL id {} {} {}", stopLoss.orderId(), contract.symbol(), stopLossPrice);
	}

	public OrderBracketIds placeOrderWithBracket(ContractDetails contractDetails,
	                                             long quantity,
	                                             Double stop,
	                                             Double limit,
	                                             Double stopLossPrice,
	                                             Double takeProfitPrice,
	                                             Action action,
	                                             OrderType orderType, Consumer<Boolean> mainExecutionListener) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}

		Order main = prepareOrder(quantity, stop, limit, action, orderType, false);

		Order stopLoss = new Order();
		stopLoss.action(action == Action.SELL ? Action.BUY : Action.SELL);
		stopLoss.orderType(OrderType.STP);
		//Stop trigger price
		stopLoss.auxPrice(round(stopLossPrice));
		stopLoss.totalQuantity(Decimal.get(quantity));
		// In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to
		// true to activate all its predecessors
		stopLoss.transmit(true);

		ibkrConnectionHandler.controller()
		                     .placeOrModifyOrder(contractDetails.contract(),
		                                         main,
		                                         new IbkrOrderHandler(contractDetails.contract(),
		                                                              main, mainExecutionListener));

		if (orderType == OrderType.STP_LMT) {
			log.info("Placed main {} {} {}, STP: {}, LMT: {}, QTT: {}, SL: {}, TP: {}",
			         main.orderId(),
			         main.action(),
			         contractDetails.contract().symbol(),
			         round(stop),
			         round(limit),
			         quantity,
			         round(stopLossPrice),
			         round(takeProfitPrice));
		} else {
			log.info("Placed main {} {} {}, LMT: {}, QTT: {}, SL: {}, TP: {}",
			         main.orderId(),
			         main.action(),
			         contractDetails.contract().symbol(),
			         round(limit),
			         quantity,
			         round(stopLossPrice),
			         round(takeProfitPrice));
		}

		stopLoss.parentId(main.orderId());

		ibkrConnectionHandler.controller()
		                     .placeOrModifyOrder(contractDetails.contract(),
		                                         stopLoss,
		                                         new IbkrOrderHandler(contractDetails.contract(),
		                                                              stopLoss));

		log.info("Placed SL id {} {} {}", stopLoss.orderId(), contractDetails.contract().symbol(), stopLossPrice);

		return new OrderBracketIds(String.valueOf(main.orderId()), String.valueOf(stopLoss.orderId()), null);
	}

	public void cleanAllOrders(Contract contract) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}


		log.info("Dropping all orders for {}", contract.symbol());

		ApiController.ILiveOrderHandler handler = new ApiController.ILiveOrderHandler() {
			final List<Order> orders = new ArrayList<>();

			@Override
			public void openOrder(Contract contract, Order order, OrderState orderState) {
				if (contract.conid() == contract.conid()) {
					orders.add(order);
				}
			}

			@Override
			public void openOrderEnd() {
				ibkrConnectionHandler.controller().removeLiveOrderHandler(this);
				if (TradeWindow.SIMULATION_MODE) {
					log.info("Simulation mode");
				} else {
					orders.forEach(order -> {
						ibkrConnectionHandler.controller().cancelOrder(order.orderId(), "", null);
						log.info("Dropping order {}", order.orderId());
					});
				}
			}

			@Override
			public void orderStatus(int orderId,
			                        OrderStatus status,
			                        Decimal filled,
			                        Decimal remaining,
			                        double avgFillPrice,
			                        int permId,
			                        int parentId,
			                        double lastFillPrice,
			                        int clientId,
			                        String whyHeld,
			                        double mktCapPrice) {
			}

			@Override
			public void handle(int orderId, int errorCode, String errorMsg) {
				log.error("Error dropping order {}: {} {}", orderId, errorCode, errorMsg);
			}
		};

		ibkrConnectionHandler.controller().reqLiveOrders(handler);
	}
}
