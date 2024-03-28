package com.corn.trade.service;

import com.corn.trade.BaseWindow;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.TradeRepo;
import com.corn.trade.model.ExtendedTradeContext;
import com.corn.trade.model.TradeContext;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.PositionType;
import com.corn.trade.type.TradeStatus;
import com.corn.trade.util.ExchangeTime;

import java.math.BigDecimal;

public class TradeService {

	public final TradeRepo tradeRepo;
	public final AssetService assetService;

	public TradeService() {
		this.tradeRepo = new TradeRepo();
		this.assetService = new AssetService();
	}

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

	public Trade createTrade(String assetName, String exchangeName, TradeData tradeData) throws DBException {
		Trade trade = new Trade();
		Asset asset = assetService.getAsset(assetName, exchangeName);
		Exchange exchange = asset.getExchange();
		ExchangeTime exchangeTime = new ExchangeTime(exchange);
		trade.setAsset(asset);
		trade.setType(tradeData.getPositionType().name());
		trade.setQuantity(tradeData.getQuantity());
		trade.setLimitPrice(BigDecimal.valueOf(tradeData.getOrderLimit()));
		trade.setStopLossPrice(BigDecimal.valueOf(tradeData.getOrderStop()));
		trade.setGoal(BigDecimal.valueOf(tradeData.getGoal()));
		trade.setStatus(TradeStatus.OPEN.name());
		trade.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
		tradeRepo.save(trade);
		return trade;
	}

	public void updateTrade(Long id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
