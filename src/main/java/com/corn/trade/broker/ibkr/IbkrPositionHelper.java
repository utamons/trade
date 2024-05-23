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

import com.corn.trade.type.PositionType;
import com.ib.client.*;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IbkrPositionHelper {

	public static final Logger                log = LoggerFactory.getLogger(IbkrPositionHelper.class);
	private final       IbkrConnectionHandler ibkrConnectionHandler;
	private final       IbkrBroker            ibkrBroker;

	public IbkrPositionHelper(IbkrConnectionHandler ibkrConnectionHandler, IbkrBroker ibkrBroker) {
		this.ibkrConnectionHandler = ibkrConnectionHandler;
		this.ibkrBroker = ibkrBroker;
	}

	public void dropAll() {
		if (!ibkrConnectionHandler.isConnected()) {
			log.error("Not connected");
			return;
		}

		final Contract lookedUpContract = ibkrBroker.getContractDetails().contract();
		log.info("Dropping all positions for {}", lookedUpContract.symbol());

		ApiController.ITradeReportHandler tradeReportHandler = new ApiController.ITradeReportHandler() {
			@Override
			public void tradeReport(String tradeKey, Contract contract, Execution execution) {
				log.info("Trade report: {}", tradeKey);
			}

			@Override
			public void tradeReportEnd() {
				// If trade report for our contract is not found, we should repeat requests for reasonable time
				log.info("Trade report end");
			}

			@Override
			public void commissionReport(String tradeKey, CommissionReport commissionReport) {}
		};

		ApiController.IPositionHandler handler = new ApiController.IPositionHandler() {
			@Override
			public void position(String account, Contract contract, Decimal pos, double avgCost) {
				if (pos.isZero()) {
					return;
				}

				if (contract.conid() != lookedUpContract.conid()) {
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

				ibkrConnectionHandler.controller().placeOrModifyOrder(lookedUpContract,
				                                                      closeOrder,
				                                                      new IbkrOrderHandler(lookedUpContract, closeOrder));
				log.info("Placed DROP ALL id {} {}, qtt: {}", closeOrder.orderId(), contract.symbol(), pos);

				ExecutionFilter filter = new ExecutionFilter();
				filter.symbol(lookedUpContract.symbol());
				filter.secType(lookedUpContract.getSecType());


				ibkrConnectionHandler.controller().reqExecutions(filter, tradeReportHandler);
			}

			@Override
			public void positionEnd() {
				log.info("Dropping all positions call is finished");
				ibkrConnectionHandler.controller().cancelPositions(this);
			}
		};

		ibkrConnectionHandler.controller().reqPositions(handler);
	}
}
