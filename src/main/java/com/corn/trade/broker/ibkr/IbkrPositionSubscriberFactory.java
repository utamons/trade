package com.corn.trade.broker.ibkr;

public class IbkrPositionSubscriberFactory {
	private static       IbkrPositionSubscriber positionSubscriber;

	static synchronized IbkrPositionSubscriber getPositionSubscriber() {
		if (positionSubscriber == null) {
			IbkrConnectionHandler connectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			positionSubscriber = new IbkrPositionSubscriber(connectionHandler);
		}
		return positionSubscriber;
	}
}
