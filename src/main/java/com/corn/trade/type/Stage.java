package com.corn.trade.type;

public enum Stage {
	DEV, PROD;
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
