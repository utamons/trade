package com.corn.trade.service;

import com.corn.trade.BaseWindow;
import com.corn.trade.model.ExtendedTradeContext;
import com.corn.trade.model.TradeContext;
import com.corn.trade.type.PositionType;

public class TradeService {
	public ExtendedTradeContext getExtendedTradeContext(TradeContext tradeContext, PositionType positionType) {
		Double ask   = tradeContext.getAsk();
		Double bid   = tradeContext.getBid();
		Double price = tradeContext.getPrice();
		Double high  = tradeContext.getDayHigh();
		Double low   = tradeContext.getDayLow();
		Double adr   = tradeContext.getAdr();

		if (price == null || high == null || low == null || adr == null || ask == null || bid == null) {
			return null;
		}

		double slippage = getSlippage();

		double range = high - low;

		double spread = ask - bid;

		double adrUsed = range / adr * 100;

		// Ideally all goals should be within this range
		double maxRange = adrUsed > 100 ? range : adr;

		double fromHigh        = high - price;
		double fromLow         = price - low;
		double passed          = positionType == PositionType.LONG ? fromLow : fromHigh;
		double maxRangePassedForPos = (passed / maxRange) * 100;
		double maxRangeLeftForPos   = 100 - maxRangePassedForPos;

		return ExtendedTradeContext.ExtendedTradeContextBuilder.anExtendedTradeContext()
			.withTradeContext(tradeContext)
			.withSlippage(slippage)
			.withSpread(spread)
			.withMaxRange(maxRange)
			.withMaxRangePassedForPos(maxRangePassedForPos)
			.withMaxRangeLeftForPos(maxRangeLeftForPos)
			.build();
	}

	private double getSlippage() {
		// todo: Implement slippage calculation
		return BaseWindow.ORDER_LUFT;
	}
}
