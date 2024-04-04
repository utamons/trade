package com.corn.trade.broker.ibkr;

@SuppressWarnings("SameReturnValue")
interface IConnectionConfiguration {
	String getDefaultHost();
	int getDefaultPort();
	String getDefaultConnectOptions();

	class DefaultConnectionConfiguration implements IConnectionConfiguration {
		@Override public String getDefaultHost() { return "127.0.0.1"; }
		@Override public int getDefaultPort() { return 7496; }
		@Override public String getDefaultConnectOptions() { return null; }
	}

	class PaperConnectionConfiguration implements IConnectionConfiguration {
		@Override
		public String getDefaultHost() {
			return "127.0.0.1";
		}

		@Override
		public int getDefaultPort() {
			return 7497;
		}

		@Override
		public String getDefaultConnectOptions() {
			return null;
		}
	}
}
