package com.corn.trade.broker.ibkr;

public class IbkrException extends RuntimeException {
	public IbkrException(String message) {
		super(message);
	}

	public IbkrException(Throwable cause) {
		super(cause);
	}
}
