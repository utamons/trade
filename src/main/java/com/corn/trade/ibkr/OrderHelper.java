package com.corn.trade.ibkr;

import com.corn.trade.trade.OrderAction;
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

		ibkr.placeOrder(contractDetails.contract(), parent, new OrderHandler(contractDetails.contract(), parent, OrderAction.MAIN, quantityDecimal, positionType));

		log.info("Placed main id {} {} {}, stop price: {}, limit price: {}, quantity: {}, stop loss: {}, take profit: {}",
				  parent.orderId(),
				  positionType,
		          contractDetails.contract().symbol(),
		          stop,
		          limit,
		          quantityDecimal,
		          stopLossPrice,
		          takeProfitPrice);

		takeProfit.parentId(parent.orderId());
		stopLoss.parentId(parent.orderId());

		ibkr.placeOrder(contractDetails.contract(), takeProfit, new OrderHandler(contractDetails.contract(), takeProfit, OrderAction.TAKE_PROFIT, quantityDecimal, positionType));

		log.info("Placed TP id {} {} {}",
		          takeProfit.orderId(),
		          contractDetails.contract().symbol(),
		          takeProfitPrice);

		ibkr.placeOrder(contractDetails.contract(), stopLoss, new OrderHandler(contractDetails.contract(), stopLoss, OrderAction.STOP_LOSS, quantityDecimal, positionType));

		log.info("Placed SL id {} {} {}",
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
