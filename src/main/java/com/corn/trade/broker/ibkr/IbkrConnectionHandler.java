package com.corn.trade.broker.ibkr;

import com.corn.trade.type.Stage;
import com.corn.trade.util.Trigger;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.ContractLookuper;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.corn.trade.BaseWindow.STAGE;

class IbkrConnectionHandler implements IConnectionHandler {
	private static final org.slf4j.Logger         log                     =
			LoggerFactory.getLogger(IbkrConnectionHandler.class);
	private final        IConnectionConfiguration connectionConfiguration = STAGE ==
	                                                                        Stage.DEV ?
			new IConnectionConfiguration.PaperConnectionConfiguration() :
			new IConnectionConfiguration.DefaultConnectionConfiguration();
	private final        List<Trigger>            disconnectionListeners  = new ArrayList<>();
	private              IbkrApiController        controller;
	private final        ContractLookuper         lookuper                =
			contract -> com.ib.client.Util.lookupContract(controller(), contract);
	private              List<String>             accountList;

	/**
	 * Initiates connection to TWS. It's an asynchronous process, so connection might not be ready immediately.
	 *
	 * @see IbkrConnectionChecker
	 */
	void run() {
		controller().connect(connectionConfiguration.getDefaultHost(),
		                     connectionConfiguration.getDefaultPort(),
		                     0,
		                     connectionConfiguration.getDefaultConnectOptions());
	}

	/**
	 * Adds a listener, which detects disconnection
	 *
	 * @param disconnectionListener listener
	 */
	void setDisconnectionListener(Trigger disconnectionListener) {
		this.disconnectionListeners.add(disconnectionListener);
	}

	boolean isConnected() {
		return ((IbkrApiController) controller()).isConnected();
	}

	List<ContractDetails> lookupContract(Contract contract) {
		log.debug("lookupContract start");
		if (!isConnected()) throw new IbkrException("IBKR not connected");
		List<ContractDetails> contractDetails = lookuper.lookupContract(contract);
		log.debug("lookupContract finish");
		return contractDetails;
	}

	List<String> getAccountList() {
		return accountList;
	}

	@Override
	public void connected() {
		show("connected");
	}

	@Override
	public void disconnected() {
		show("disconnected");
		disconnectionListeners.forEach(Trigger::trigger);
	}

	@Override
	public void accountList(List<String> list) {
		accountList = list;
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

	ApiController controller() {
		if (controller == null) {
			controller = new IbkrApiController(this, new Logger(), new Logger());
		}
		return controller;
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
