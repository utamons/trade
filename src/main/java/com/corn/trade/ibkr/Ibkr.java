package com.corn.trade.ibkr;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.ContractLookuper;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.Formats;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Ibkr implements IConnectionHandler {

	public static org.slf4j.Logger log = LoggerFactory.getLogger(Ibkr.class);

	private final IConnectionConfiguration m_connectionConfiguration =
			new IConnectionConfiguration.DefaultConnectionConfiguration();
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final List<String>             m_acctList                = new ArrayList<>();
	private       ApiController    m_controller;
	private final ContractLookuper m_lookuper = contract -> com.ib.client.Util.lookupContract(controller(), contract);

	public void run() {
		controller().connect(
				m_connectionConfiguration.getDefaultHost(),
				m_connectionConfiguration.getDefaultPort(),
				0,
				m_connectionConfiguration.getDefaultConnectOptions());
	}

	public boolean isConnected() {
		return controller().client().isConnected();
	}

	public List<ContractDetails> lookupContract(Contract contract) {
		return m_lookuper.lookupContract(contract);
	}

	@Override
	public void connected() {
		show("connected");

		controller().reqCurrentTime(time -> show("Server date/time is " + Formats.fmtDate(time * 1000)));

		controller().reqBulletins(true, (msgId, newsType, message, exchange) -> {
			String str = String.format("Received bulletin:  type=%s  exchange=%s", newsType, exchange);
			show(str);
			show(message);
		});
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
		show(e.toString());
	}

	@Override
	public void message(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String error = id + " " + errorCode + " " + errorMsg;
		if (advancedOrderRejectJson != null) {
			error += (" " + advancedOrderRejectJson);
		}
		show(error);
	}

	@Override
	public void show(String str) {
		log.debug(str);
	}

	public ApiController controller() {
		if (m_controller == null) {
			m_controller = new ApiController(this, new Logger(), new Logger());
		}
		return m_controller;
	}

	private static class Logger implements ApiConnection.ILogger {
		@Override
		public void log(final String str) {
			log.debug(str);
		}
	}
}
