package com.corn.trade.ibkr;

import com.corn.trade.util.Util;
import com.ib.client.*;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.Formats;

import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.util.Util.log;

public class Ibkr implements IConnectionHandler  {

	private final IConnectionConfiguration m_connectionConfiguration = new IConnectionConfiguration.DefaultConnectionConfiguration();
	private ApiController m_controller;
	private final List<String> m_acctList = new ArrayList<>();

	private ContractLookuper m_lookuper = contract -> com.ib.client.Util.lookupContract(controller(), contract);

	public void run() {
		controller().connect(
				m_connectionConfiguration.getDefaultHost(),
				m_connectionConfiguration.getDefaultPort(),
				0,
				m_connectionConfiguration.getDefaultConnectOptions() );
	}

	public List<ContractDetails> lookupContract(Contract contract) {
		return m_lookuper.lookupContract(contract);
	}

	@Override public void connected() {
		show( "connected");

		controller().reqCurrentTime(time -> show("Server date/time is " + Formats.fmtDate(time * 1000) ));

		controller().reqBulletins( true, (msgId, newsType, message, exchange) -> {
			String str = String.format( "Received bulletin:  type=%s  exchange=%s", newsType, exchange);
			show( str);
			show( message);
		});

		reqExecutions();
	}

	@Override public void disconnected() {
		show( "disconnected");
	}

	@Override public void accountList(List<String> list) {
		show( "Received account list");
		m_acctList.clear();
		m_acctList.addAll( list);
	}

	@Override
	public void error(Exception e) {
		show( e.toString() );
	}

	@Override public void message(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String error = id + " " + errorCode + " " + errorMsg;
		if (advancedOrderRejectJson != null) {
			error += (" " + advancedOrderRejectJson);
		}
		show(error);
	}

	@Override
	public void show(String str) {
		log(str);
	}

	public ApiController controller() {
		if ( m_controller == null ) {
			m_controller = new ApiController( this, new Logger(), new Logger() );
		}
		return m_controller;
	}

	private static class Logger implements ApiConnection.ILogger {
		@Override public void log(final String str) {
			Util.log(str);
		}
	}

	public void reqExecutions() {
		controller().reqCompletedOrders(new ApiController.ICompletedOrdersHandler() {
			@Override
			public void completedOrder(Contract contract, Order order, OrderState orderState) {
				System.out.println("contract: " + contract);
				System.out.println("order type: " + order.getOrderType());
				System.out.println("orderState: " + orderState.completedStatus());
			}

			@Override
			public void completedOrdersEnd() {
				System.out.println("completedOrdersEnd");
			}
		});
	}
}
