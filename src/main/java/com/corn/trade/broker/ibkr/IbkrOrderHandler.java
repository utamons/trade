package com.corn.trade.broker.ibkr;

import com.corn.trade.util.ChangeOrderListener;
import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.corn.trade.broker.ibkr.IbkrBroker.fromOrderStatus;

class IbkrOrderHandler implements ApiController.IOrderHandler {
	public static final Logger              log       = LoggerFactory.getLogger(IbkrOrderHandler.class);
	private final       Contract            contract;
	private final       Order               order;
	private final       ChangeOrderListener changeOrderListener;

	public IbkrOrderHandler(Contract contract, Order order, ChangeOrderListener changeOrderListener) {
		this.contract = contract;
		this.order = order;
		this.changeOrderListener = changeOrderListener;
	}

	private String orderInfo() {
		String parentOrderId = order.parentId() == 0 ? "" : " parent order id: " + order.parentId();
		return "order id" +
		       order.orderId() +
		       parentOrderId +
		       " " +
		       contract.symbol() +
		       " " +
		       order.action() +
		       " " +
		       order.orderType() +
		       " qtt: " +
		       order.totalQuantity() +
		       " price: " +
		       order.lmtPrice() +
		       " auxPrice: " +
		       order.auxPrice();
	}

	@Override
	public void orderState(OrderState orderState) {
	}

	@Override
	public void orderStatus(OrderStatus status,
	                        Decimal filled,
	                        Decimal remaining,
	                        double avgFillPrice,
	                        int permId,
	                        int parentId,
	                        double lastFillPrice,
	                        int clientId,
	                        String whyHeld,
	                        double mktCapPrice) {
		log.info("{} status: {}", orderInfo(), status);
		if (changeOrderListener == null) {
			return;
		}
		changeOrderListener.onOrderChange(order.orderId(),
		                                  parentId,
		                                  fromOrderStatus(status),
		                                  filled.longValue(),
		                                  remaining.longValue(),
		                                  avgFillPrice);
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		log.error("{} errorCode: {}, errorMsg: {}", orderInfo(), errorCode, errorMsg);
		if (changeOrderListener == null) {
			return;
		}
		changeOrderListener.onOrderError(order.orderId(), String.valueOf(errorCode), errorMsg);
	}
}
