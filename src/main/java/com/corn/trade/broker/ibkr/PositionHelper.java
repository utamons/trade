package com.corn.trade.broker.ibkr;

import com.corn.trade.trade.type.OrderAction;
import com.corn.trade.trade.type.PositionType;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PositionHelper {

	public static final Logger      log = LoggerFactory.getLogger(PositionHelper.class);
	private final IbkrAdapter ibkrAdapter;
	private final IbkrBroker  ibkrBroker;

	public PositionHelper(IbkrAdapter ibkrAdapter, IbkrBroker ibkrBroker) {
		this.ibkrAdapter = ibkrAdapter;
		this.ibkrBroker = ibkrBroker;
	}

	public void dropAll() {
		log.info("Dropping all positions");

		if (!ibkrAdapter.isConnected()) {
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

				Contract lookedUpContract = ibkrBroker.getContractDetails().contract();

				ibkrAdapter.placeOrder(lookedUpContract,
				                       closeOrder,
				                       new OrderHandler(lookedUpContract, closeOrder, OrderAction.DROP_ALL, pos, positionType));
				log.info("Placed DROP ALL id {} {}, qtt: {}", closeOrder.orderId(), contract.symbol(), pos);
			}

			@Override
			public void positionEnd() {
				log.info("Dropping all positions call is finished");
				ibkrAdapter.controller().cancelPositions(this);
			}
		};

		ibkrAdapter.controller().reqPositions(handler);
	}
}
