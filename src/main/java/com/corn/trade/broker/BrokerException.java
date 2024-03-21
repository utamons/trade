package com.corn.trade.broker;

public class BrokerException extends Exception {
	public BrokerException(String message) {
		super(message);
	}
	public BrokerException(String message, Throwable cause) {
		super(message, cause);
	}
}
