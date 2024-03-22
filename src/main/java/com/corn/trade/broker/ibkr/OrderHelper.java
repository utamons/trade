package com.corn.trade.broker.ibkr;

import com.corn.trade.TradeWindow;
import com.corn.trade.trade.type.OrderAction;
import com.corn.trade.trade.type.PositionType;
import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.round;

class OrderHelper {

	private static final Logger                log = org.slf4j.LoggerFactory.getLogger(OrderHelper.class);
	private final        IbkrConnectionHandler ibkrConnectionHandler;

	public OrderHelper(IbkrConnectionHandler ibkrConnectionHandler) {
		this.ibkrConnectionHandler = ibkrConnectionHandler;
	}

	public void placeOrder(ContractDetails contractDetails,
	                       long quantity,
	                       Double stop,
	                       Double limit,
	                       Double stopLossPrice,
	                       Double takeProfitPrice,
	                       Double breakEvenPrice,
	                       PositionType positionType) {
		if (!ibkrConnectionHandler.isConnected()) {
			log.error("Not connected");
			return;
		}

		Order parent = new Order();
		parent.action(positionType == PositionType.LONG ? "BUY" : "SELL");
		if (stop == null) {
			parent.orderType("LMT");
		} else {
			parent.orderType("STP LMT");
		}
		Decimal quantityDecimal = Decimal.get(quantity);
		parent.totalQuantity(quantityDecimal);
		parent.lmtPrice(round(limit));
		if (stop != null) {
			parent.auxPrice(round(stop));
		}
		parent.transmit(false);

		Order takeProfit = new Order();
		takeProfit.action(positionType == PositionType.LONG ? "SELL" : "BUY");
		takeProfit.orderType("LMT");
		takeProfit.totalQuantity(quantityDecimal);
		takeProfit.lmtPrice(round(takeProfitPrice));
		takeProfit.transmit(false);

		Order stopLoss = new Order();
		stopLoss.action(positionType == PositionType.LONG ? "SELL" : "BUY");
		stopLoss.orderType("STP");
		//Stop trigger price
		stopLoss.auxPrice(round(stopLossPrice));
		stopLoss.totalQuantity(quantityDecimal);
		//In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to
		// true
		//to activate all its predecessors
		stopLoss.transmit(true);

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      parent,
		                                                      new OrderHandler(contractDetails.contract(),
		                                 parent,
		                                 OrderAction.MAIN,
		                                 quantityDecimal,
		                                 positionType));

		if (stop != null) {
			log.info("Placed main {} {} {}, STP: {}, LMT: {}, QTT: {}, SL: {}, TP: {}, BE: {}",
			         parent.orderId(),
			         positionType,
			         contractDetails.contract().symbol(),
			         round(stop),
			         round(limit),
			         quantityDecimal,
			         round(stopLossPrice),
			         round(takeProfitPrice),
			         round(breakEvenPrice));
		} else {
			log.info("Placed main {} {} {}, LMT: {}, QTT: {}, SL: {}, TP: {}, BE: {}",
			         parent.orderId(),
			         positionType,
			         contractDetails.contract().symbol(),
			         round(limit),
			         quantityDecimal,
			         round(stopLossPrice),
			         round(takeProfitPrice),
			         round(breakEvenPrice));
		}

		takeProfit.parentId(parent.orderId());
		stopLoss.parentId(parent.orderId());

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      takeProfit,
		                                                      new OrderHandler(contractDetails.contract(),
		                                 takeProfit,
		                                 OrderAction.TAKE_PROFIT,
		                                 quantityDecimal,
		                                 positionType));

		log.info("Placed TP id {} {} {}",
		         takeProfit.orderId(),
		         contractDetails.contract().symbol(),
		         takeProfitPrice);

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      stopLoss,
		                                                      new OrderHandler(contractDetails.contract(),
		                                 stopLoss,
		                                 OrderAction.STOP_LOSS,
		                                 quantityDecimal,
		                                 positionType));

		log.info("Placed SL id {} {} {}",
		         stopLoss.orderId(),
		         contractDetails.contract().symbol(),
		         stopLossPrice);
	}

	public void dropAll(PositionHelper positionHelper) {
		if (!ibkrConnectionHandler.isConnected()) {
			log.error("Not connected");
			return;
		}

		log.info("Dropping all orders");

		ApiController.ILiveOrderHandler handler = new ApiController.ILiveOrderHandler() {
			final List<Order> orders = new ArrayList<>();

			@Override
			public void openOrder(Contract contract, Order order, OrderState orderState) {
				orders.add(order);
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
				positionHelper.dropAll();
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
