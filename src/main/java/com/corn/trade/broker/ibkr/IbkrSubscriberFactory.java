package com.corn.trade.broker.ibkr;

public class IbkrSubscriberFactory {
	private static       IbkrPositionSubscriber positionSubscriber;
	private static       IbkrPnLSubscriber      pnlSubscriber;


	static synchronized IbkrPositionSubscriber getPositionSubscriber() {
		if (positionSubscriber == null) {
			IbkrConnectionHandler connectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			positionSubscriber = new IbkrPositionSubscriber(connectionHandler);
		}
		return positionSubscriber;
	}

	static synchronized IbkrPnLSubscriber getPnlSubscriber() {
		if (pnlSubscriber == null) {
			IbkrConnectionHandler connectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			pnlSubscriber = new IbkrPnLSubscriber(connectionHandler);
		}
		return pnlSubscriber;
	}
}
