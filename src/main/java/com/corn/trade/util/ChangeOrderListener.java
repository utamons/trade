package com.corn.trade.util;

import com.corn.trade.type.OrderStatus;

@FunctionalInterface
public interface ChangeOrderListener {
	void onOrderChange(
			long id,
			OrderStatus status,
			double filled,
            double remaining,
			double avgFillPrice
			);
}
