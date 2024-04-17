package com.corn.trade.type;

public enum Stage {
	DEV, PROD, SIMULATION;
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
