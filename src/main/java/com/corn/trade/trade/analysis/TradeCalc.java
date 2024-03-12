package com.corn.trade.trade.analysis;

import com.corn.trade.trade.EstimationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeCalc {
	private final Logger log = LoggerFactory.getLogger(TradeCalc.class);

	public void calculate(TradeData tradeData) {
		validate(tradeData);
	}

	public void validate(TradeData tradeData) {
		if (tradeData.getLevel() == null) {
			throw new IllegalArgumentException("Level is required");
		}
		if (tradeData.getPrice() == null) {
			throw new IllegalArgumentException("Price is required");
		}
		if (tradeData.getEstimationType() != EstimationType.MIN_GOAL && (tradeData.getPowerReserve() == null || tradeData.getGoal() == null)) {
			throw new IllegalArgumentException("Goal or Power reserve is required");
		}
	}

}
