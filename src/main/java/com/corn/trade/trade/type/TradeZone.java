package com.corn.trade.trade.type;

public enum TradeZone {
	SHORT, LONG, NEUTRAL;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
