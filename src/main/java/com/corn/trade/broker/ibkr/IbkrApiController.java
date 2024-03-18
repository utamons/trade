package com.corn.trade.broker.ibkr;

import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;

public class IbkrApiController extends ApiController {
	public IbkrApiController(IConnectionHandler handler, ApiConnection.ILogger inLogger, ApiConnection.ILogger outLogger) {
		super(handler, inLogger, outLogger);
	}

	public boolean isConnected() {
		return client().isConnected() && checkConnection();
	}
}
