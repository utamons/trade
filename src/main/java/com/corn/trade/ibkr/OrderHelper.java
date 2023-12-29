package com.corn.trade.ibkr;

import com.corn.trade.trade.PositionType;
import com.ib.client.*;
import org.slf4j.Logger;

import static com.corn.trade.util.Util.round;

public class OrderHelper {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(OrderHelper.class);
	private final        Ibkr   ibkr;

	public OrderHelper(Ibkr ibkr) {
		this.ibkr = ibkr;
	}

	public void placeOrder(ContractDetails contractDetails,
	                       long quantity,
	                       Double stop,
	                       Double limit,
	                       Double stopLossPrice,
	                       Double takeProfitPrice,
	                       PositionType positionType) {
		if (!ibkr.isConnected()) {
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
		//In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to true
		//to activate all its predecessors
		stopLoss.transmit(true);

		ibkr.placeOrder(contractDetails.contract(), parent);

		log.info("Placed a main order id {} for {}, stop price: {}, limit price: {}",
		          parent.orderId(),
		          contractDetails.contract().symbol(),
		          stop,
		          limit);

		takeProfit.parentId(parent.orderId());
		stopLoss.parentId(parent.orderId());

		ibkr.placeOrder(contractDetails.contract(), takeProfit);

		log.info("Placed a take profit order id {} for {}, take profit price: {}",
		          takeProfit.orderId(),
		          contractDetails.contract().symbol(),
		          takeProfitPrice);

		ibkr.placeOrder(contractDetails.contract(), stopLoss);

		log.info("Placed a stop loss order id {} for {}, stop loss price: {}",
		          stopLoss.orderId(),
		          contractDetails.contract().symbol(),
		          stopLossPrice);
	}

	public void dropAll() {
		if (!ibkr.isConnected()) {
			log.error("Not connected");
			return;
		}
		ibkr.controller().cancelAllOrders();
	}
}
