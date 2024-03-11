package com.corn.trade.trade.analysis;

import com.corn.trade.trade.PositionType;

public class TradeCalc {

	private TradeContext context;

	public TradeCalc(TradeContext context) {
		this.context = context;
	}

	public void calculate() {
		TradeContextData data = context.getData();
		PositionType positionType = data.getPositionType();
	}

}
