package com.corn.trade.jpa;

import com.corn.trade.entity.Trade;

public class TradeRepo extends JpaRepo<Trade, Long> {
	public TradeRepo() {
		super(Trade.class);
	}
}
