package com.corn.trade.ibkr;

public interface IConnectionConfiguration {
	String getDefaultHost();
	int getDefaultPort();
	String getDefaultConnectOptions();

	/** Standard ApiDemo configuration for pre-v100 connection */
	class DefaultConnectionConfiguration implements IConnectionConfiguration {
		@Override public String getDefaultHost() { return "127.0.0.1"; }
		@Override public int getDefaultPort() { return 7496; }
		@Override public String getDefaultConnectOptions() { return null; }
	}
}
