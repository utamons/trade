package com.corn.trade.broker.ibkr;

import com.corn.trade.TradeWindow;
import com.ib.client.*;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.round;

class IbkrOrderHelper {

	private static final Logger                log = org.slf4j.LoggerFactory.getLogger(IbkrOrderHelper.class);
	private final        IbkrConnectionHandler ibkrConnectionHandler;
	private final       IbkrBroker            ibkrBroker;

	public IbkrOrderHelper(IbkrConnectionHandler ibkrConnectionHandler, IbkrBroker ibkrBroker) {
		this.ibkrConnectionHandler = ibkrConnectionHandler;
		this.ibkrBroker = ibkrBroker;
	}

	public void placeOrderWithBracket(ContractDetails contractDetails,
	                                  long quantity,
	                                  Double stop,
	                                  Double limit,
	                                  Double stopLossPrice,
	                                  Double takeProfitPrice,
	                                  Action action,
	                                  OrderType orderType) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}

		Order parent = new Order();
		parent.action(action);
		parent.orderType(orderType);

		Decimal quantityDecimal = Decimal.get(quantity);
		parent.totalQuantity(quantityDecimal);
		parent.lmtPrice(round(limit));
		if (stop != null) {
			parent.auxPrice(round(stop));
		}
		parent.transmit(false);

		Order takeProfit = new Order();
		takeProfit.action(action == Action.SELL ? Action.BUY : Action.SELL);
		takeProfit.orderType(OrderType.LMT);
		takeProfit.totalQuantity(quantityDecimal);
		takeProfit.lmtPrice(round(takeProfitPrice));
		takeProfit.transmit(false);

		Order stopLoss = new Order();
		stopLoss.action(action == Action.SELL ? Action.BUY : Action.SELL);
		stopLoss.orderType(OrderType.STP);
		//Stop trigger price
		stopLoss.auxPrice(round(stopLossPrice));
		stopLoss.totalQuantity(quantityDecimal);
		// In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to
		// true to activate all its predecessors
		stopLoss.transmit(true);

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      parent,
		                                                      new IbkrOrderHandler(contractDetails.contract(),
		                                                                           parent));

		if (orderType == OrderType.STP_LMT) {
			log.info("Placed main {} {} {}, STP: {}, LMT: {}, QTT: {}, SL: {}, TP: {}",
			         parent.orderId(),
					 parent.action(),
			         contractDetails.contract().symbol(),
			         round(stop),
			         round(limit),
			         quantityDecimal,
			         round(stopLossPrice),
			         round(takeProfitPrice));
		} else {
			log.info("Placed main {} {} {}, LMT: {}, QTT: {}, SL: {}, TP: {}",
			         parent.orderId(),
			         parent.action(),
			         contractDetails.contract().symbol(),
			         round(limit),
			         quantityDecimal,
			         round(stopLossPrice),
			         round(takeProfitPrice));
		}

		takeProfit.parentId(parent.orderId());
		stopLoss.parentId(parent.orderId());

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      takeProfit,
		                                                      new IbkrOrderHandler(contractDetails.contract(),
		                                                                           takeProfit));

		log.info("Placed TP id {} {} {}",
		         takeProfit.orderId(),
		         contractDetails.contract().symbol(),
		         takeProfitPrice);

		ibkrConnectionHandler.controller().placeOrModifyOrder(contractDetails.contract(),
		                                                      stopLoss,
		                                                      new IbkrOrderHandler(contractDetails.contract(),
		                                                                           stopLoss));

		log.info("Placed SL id {} {} {}",
		         stopLoss.orderId(),
		         contractDetails.contract().symbol(),
		         stopLossPrice);
	}

	public void dropAll(IbkrPositionHelper ibkrPositionHelper) {
		if (!ibkrConnectionHandler.isConnected()) {
			throw new IbkrException("IBKR not connected");
		}
		final Contract lookedUpContract = ibkrBroker.getContractDetails().contract();

		log.info("Dropping all orders for {}", lookedUpContract.symbol());

		ApiController.ILiveOrderHandler handler = new ApiController.ILiveOrderHandler() {
			final List<Order> orders = new ArrayList<>();

			@Override
			public void openOrder(Contract contract, Order order, OrderState orderState) {
				if (contract.conid() == lookedUpContract.conid()) {
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
				ibkrPositionHelper.dropAll();
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
