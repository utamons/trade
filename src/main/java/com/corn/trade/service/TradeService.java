/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.service;

import com.corn.trade.BaseWindow;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.AssetRepo;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.ExchangeRepo;
import com.corn.trade.jpa.TradeRepo;
import com.corn.trade.model.ExtendedTradeContext;
import com.corn.trade.model.TradeContext;
import com.corn.trade.model.TradeData;
import com.corn.trade.type.PositionType;
import com.corn.trade.type.TradeStatus;
import com.corn.trade.util.ExchangeTime;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static com.corn.trade.util.Util.toBigDecimal;

/*
  The class is not intended to be shared across threads, it is not thread-safe!
 */
public class TradeService extends BaseService {

	private static final Logger log                       = LoggerFactory.getLogger(TradeService.class);
	public static final double     MAX_RANGE_COEFF = 0.8;
	private final        TradeRepo tradeRepo;
	private final        AssetRepo    assetRepo;
	private final        ExchangeRepo exchangeRepo;

	public TradeService() {
		this.tradeRepo = new TradeRepo();
		this.assetRepo = new AssetRepo();
		this.exchangeRepo = new ExchangeRepo();
		addRepo(tradeRepo);
		addRepo(assetRepo);
		addRepo(exchangeRepo);
	}

	public ExtendedTradeContext getExtendedTradeContext(TradeContext tradeContext, PositionType positionType, Exchange exchange) {
		Double ask   = tradeContext.getAsk();
		Double bid   = tradeContext.getBid();
		Double price = tradeContext.getPrice();
		Double high  = tradeContext.getDayHigh();
		Double low   = tradeContext.getDayLow();
		Double adr   = tradeContext.getAdr();
		ExchangeTime exchangeTime = new ExchangeTime(exchange);

		if (price == null || high == null || low == null || adr == null || ask == null || bid == null) {
			return null;
		}

		double slippage = getSlippage();

		double range = high - low;

		double spread = ask - bid;

		// Ideally all goals should be within this range
		double maxRange = Math.max(range, adr * MAX_RANGE_COEFF);

		double fromHigh             = high - price;
		double fromLow              = price - low;
		double passed               = positionType == PositionType.LONG ? fromLow : fromHigh;
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

	public Trade getOpenTrade(String assetName, String exchangeName) throws DBException {
		try {
			Exchange exchange =
					exchangeRepo.findExchange(exchangeName).orElseThrow(() -> new DBException("Exchange not found"));
			Asset asset = assetRepo.findAsset(assetName, exchange).orElseThrow(() -> new DBException("Asset not found"));
			return tradeRepo.getOpenTrade(asset);
		} catch (PersistenceException e) {
			throw new DBException(e);
		}
	}

	public Trade saveNewTrade(String assetName, String exchangeName, TradeData tradeData) throws DBException {
		beginTransaction();
		try {

			Exchange exchange =
					exchangeRepo.findExchange(exchangeName).orElseThrow(() -> new DBException("Exchange not found"));
			Asset asset = assetRepo.findAsset(assetName, exchange).orElseThrow(() -> new DBException("Asset not found"));
			ExchangeTime exchangeTime = new ExchangeTime(exchange);

			Trade existingTrade = getOpenTrade(assetName, exchangeName);

			if (existingTrade != null) {
				log.debug("Trade already exists for asset {}", assetName);
				return existingTrade;
			}

			Trade trade = new Trade();
			trade.setAsset(asset);
			trade.setType(tradeData.getPositionType().name());
			trade.setQuantity(tradeData.getQuantity());
			trade.setInitialPrice(toBigDecimal(tradeData.getOrderLimit()));
			trade.setStopLossPrice(toBigDecimal(tradeData.getStopLoss()));
			trade.setBreakEvenPrice(toBigDecimal(tradeData.getBreakEven()));
			trade.setGoal(toBigDecimal(tradeData.getTarget()));
			trade.setStatus(TradeStatus.OPEN.name());
			trade.setCreatedAt(exchangeTime.nowInExchangeTZ().toLocalDateTime());
			tradeRepo.save(trade);
			commitTransaction();
			return trade;
		} catch (DBException e) {
			rollbackTransaction();
			throw e;
		}
	}

	public Trade updateTradeStatus(long tradeId, TradeStatus status) throws DBException {
		beginTransaction();
		try {
			Trade trade = tradeRepo.findById(tradeId).orElseThrow(() -> new DBException("Trade not found"));
			trade.setStatus(status.name());
			if (status == TradeStatus.CLOSED) {
				ExchangeTime exchangeTime = new ExchangeTime(trade.getAsset().getExchange());
				LocalDateTime localDateTime = exchangeTime.nowInExchangeTZ().toLocalDateTime();
				trade.setClosedAt(localDateTime);
				log.debug("Closing trade {} at {}", tradeId, localDateTime);
			}
			commitTransaction();
			return trade;
		} catch (DBException e) {
			rollbackTransaction();
			throw e;
		}
	}
}
