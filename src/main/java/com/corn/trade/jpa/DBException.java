package com.corn.trade.jpa;

import jakarta.persistence.PersistenceException;

public class DBException extends Exception {
	public DBException(PersistenceException exception) {
		super(exception.getMessage(), exception);
	}

	public DBException(String message) {
		super(message);
	}
}
