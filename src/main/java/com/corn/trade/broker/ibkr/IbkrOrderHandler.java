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
package com.corn.trade.broker.ibkr;

import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static com.corn.trade.broker.ibkr.IbkrBroker.fromOrderStatus;

class IbkrOrderHandler implements ApiController.IOrderHandler {
	public static final Logger            log = LoggerFactory.getLogger(IbkrOrderHandler.class);
	private final       Contract          contract;
	private final       Order             order;
	private final       Consumer<com.corn.trade.type.OrderStatus> executionListener;

	public IbkrOrderHandler(Contract contract, Order order) {
		this.contract = contract;
		this.order = order;
		this.executionListener = null;
	}

	public IbkrOrderHandler(Contract contract, Order order, Consumer<com.corn.trade.type.OrderStatus> executionListener) {
		this.contract = contract;
		this.order = order;
		this.executionListener = executionListener;
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
		log.info(
				"{} status: {}, filled: {}, remaining: {}, avgFillPrice: {}, lastFillPrice: {}, permId: {}, parentId: {}, " +
				"clientId: {}, whyHeld: {}, mktCapPrice: {}",
				orderInfo(),
				fromOrderStatus(status),
				filled,
				remaining,
				avgFillPrice,
				lastFillPrice,
				permId,
				parentId,
				clientId,
				whyHeld,
				mktCapPrice);
		if (executionListener != null) {
			executionListener.accept(fromOrderStatus(status));
		}
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		log.error("{} errorCode: {}, errorMsg: {}", orderInfo(), errorCode, errorMsg);
	}
}
