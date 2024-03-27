package com.corn.trade.broker.ibkr;

import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IbkrOrderHandler implements ApiController.IOrderHandler {
	public static final Logger   log = LoggerFactory.getLogger(IbkrOrderHandler.class);
	private final       Contract contract;
	private final Order     order;
	private final long startTime = System.currentTimeMillis();
	public IbkrOrderHandler(Contract contract, Order order) {
		this.contract = contract;
		this.order = order;
	}

	private String orderInfo() {
		String parentOrderId = order.parentId() == 0 ? "" : " parent order id: " + order.parentId();
		return "order id" + order.orderId() + parentOrderId + " " + contract.symbol() + " " + order.action() + " " + order.orderType() + " qtt: " + order.totalQuantity() + " price: " + order.lmtPrice() + " auxPrice: " + order.auxPrice();
	}

	@Override
	public void orderState(OrderState orderState) {}

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
		long endTime = System.currentTimeMillis();
		if (status == OrderStatus.Submitted) {
			log.info("{} submitted in {} ms", orderInfo(), endTime - startTime);
		}
		if (status == OrderStatus.Filled) {
			log.info("Order {} filled in {} ms, remaining: {}, avgFillPrice: {}, slippage {}", orderInfo(), endTime - startTime, remaining, avgFillPrice, avgFillPrice - order.lmtPrice());
		}
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		log.error("{} errorCode: {}, errorMsg: {}", orderInfo(), errorCode, errorMsg);
	}
}
