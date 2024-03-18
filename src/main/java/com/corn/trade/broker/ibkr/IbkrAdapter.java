package com.corn.trade.broker.ibkr;

import com.corn.trade.TradeWindow;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.ContractLookuper;
import com.ib.client.Order;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class IbkrAdapter implements IConnectionHandler {

	public static org.slf4j.Logger log = LoggerFactory.getLogger(IbkrAdapter.class);

	private final IConnectionConfiguration m_connectionConfiguration =
			new IConnectionConfiguration.DefaultConnectionConfiguration();
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final List<String>             m_acctList                = new ArrayList<>();
	private       IbkrApiController            m_controller;
	private final ContractLookuper m_lookuper = contract -> com.ib.client.Util.lookupContract(controller(), contract);
	private       Timer                    initTimer;
	private       Long             m_time;

	public void run() {
		controller().connect(m_connectionConfiguration.getDefaultHost(),
		                     m_connectionConfiguration.getDefaultPort(),
		                     0,
		                     m_connectionConfiguration.getDefaultConnectOptions());
	}

	public boolean isConnected() {
		return m_controller.isConnected();
	}

	public List<ContractDetails> lookupContract(Contract contract) {
		log.debug("lookupContract start");
		List<ContractDetails> contractDetails = m_lookuper.lookupContract(contract);
		log.debug("lookupContract finish");
		return contractDetails;
	}

	@Override
	public void connected() {
		show("connected");
	}

	@Override
	public void disconnected() {
		show("disconnected");
	}

	@Override
	public void accountList(List<String> list) {
		m_acctList.clear();
		m_acctList.addAll(list);
	}

	@Override
	public void error(Exception e) {
		show("error " + e.toString());
		throw new IbkrException(e.getMessage());
	}

	@Override
	public void message(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String error = id + " " + errorCode + " " + errorMsg;
		if (advancedOrderRejectJson != null) {
			error += (":" + advancedOrderRejectJson);
		}
		show("Error: " + error);
	}

	@Override
	public void show(String str) {
		log.debug(str);
	}

	public ApiController controller() {
		if (m_controller == null) {
			m_controller = new IbkrApiController(this, new Logger(), new Logger());
		}
		return m_controller;
	}

	public void placeOrder(Contract contract, Order order, ApiController.IOrderHandler handler) {
		if (TradeWindow.SIMULATION_MODE) {
			log.info("Simulation mode");
			return;
		}
		if (!isConnected()) {
			log.error("Not connected");
			return;
		}
		controller().placeOrModifyOrder(contract, order, handler);
	}

	private static class Logger implements ApiConnection.ILogger {
		@Override
		public void log(final String str) {
			/*if (!str.trim().isEmpty()) {
				log.debug("debug: " + str);
			}*/
		}
	}
}
