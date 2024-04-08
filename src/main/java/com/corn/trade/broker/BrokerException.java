package com.corn.trade.broker;

public class BrokerException extends RuntimeException {
	public BrokerException(String message) {
		super(message);
	}
	public BrokerException(String message, Throwable cause) {
		super(message, cause);
	}

	public BrokerException(Throwable cause) {
		super(cause);
	}
}
