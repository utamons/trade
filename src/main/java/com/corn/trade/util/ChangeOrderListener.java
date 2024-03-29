package com.corn.trade.util;

import com.corn.trade.type.OrderStatus;

public interface ChangeOrderListener {
	void onOrderChange(
			long id,
			OrderStatus status,
			long filled,
			long remaining,
			double avgFillPrice
			);

	void onOrderError(long id, String errorCode, String errorMsg);
}
