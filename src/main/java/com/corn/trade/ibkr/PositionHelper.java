package com.corn.trade.ibkr;

import com.corn.trade.trade.OrderAction;
import com.corn.trade.trade.PositionType;
import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionHelper {

	public static final Logger log = LoggerFactory.getLogger(PositionHelper.class);
	private final       Ibkr   ibkr;

	public PositionHelper(Ibkr ibkr) {
		this.ibkr = ibkr;
	}

	public void dropAll() {
		log.info("Dropping all positions");
		if (!ibkr.isConnected()) {
			log.error("Not connected");
			return;
		}

		ApiController.IPositionHandler handler = new ApiController.IPositionHandler() {
			@Override
			public void position(String account, Contract contract, Decimal pos, double avgCost) {
				if (pos.isZero()) {
					return;
				}
				// Determine the type of position (Long or Short)
				PositionType positionType = pos.compareTo(Decimal.ZERO) > 0 ? PositionType.LONG : PositionType.SHORT;

				// Create and send an order to close the position
				Order closeOrder = new Order();
				if (positionType == PositionType.LONG) {
					closeOrder.action("SELL");
				} else { // SHORT position
					closeOrder.action("BUY");
				}
				closeOrder.orderType("MKT"); // Market order
				closeOrder.transmit(true); // Transmit the order to IBKR
				closeOrder.totalQuantity(positionType.equals(PositionType.LONG) ? pos : pos.negate()); // Absolute quantity

				ibkr.placeOrder(contract, closeOrder, new OrderHandler(contract, closeOrder, OrderAction.DROP_ALL, pos, positionType));
				log.info("Placed DROP ALL id {} {}, qtt: {}", closeOrder.orderId(), contract.symbol(), pos);
			}

			@Override
			public void positionEnd() {
				log.info("Dropping all positions call is finished");
				ibkr.controller().cancelPositions(this);
			}
		};

		ibkr.controller().reqPositions(handler);
	}
}
