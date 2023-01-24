package com.corn.trade.mapper;

import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeLogMapper {

	public static TradeLog toEntity(TradeLogOpenDTO open,
	                                Broker broker,
	                                Market market,
	                                Ticker ticker,
	                                BigDecimal depositAmount) {
		TradeLog e = new TradeLog();
		e.setPosition(open.getPosition());
		e.setDateOpen(open.getDateOpen());
		e.setBroker(broker);
		e.setMarket(market);
		e.setTicker(ticker);
		e.setCurrency(ticker.getCurrency());
		e.setItemNumber(open.getItemNumber());
		e.setPriceOpen(open.getPriceOpen());

		BigDecimal volume = open.getPriceOpen().multiply(BigDecimal.valueOf(open.getItemNumber()));
		BigDecimal volumeToDeposit = volume.divide(depositAmount, 12, RoundingMode.HALF_EVEN)
		                                   .multiply(BigDecimal.valueOf(100.00));

		e.setVolume(volume);
		e.setVolumeToDeposit(volumeToDeposit);
		e.setStopLoss(open.getStopLoss());
		e.setTakeProfit(open.getTakeProfit());
		e.setOutcomeExpected(open.getOutcomeExpected());
		e.setRisk(open.getRisk());
		e.setFees(open.getFees() == null ? BigDecimal.ZERO : open.getFees());
		e.setNote(open.getNote());
		e.setGoal(open.getGoal());

		return e;
	}

	public static TradeLogDTO toDTO(TradeLog entity) {
		return new TradeLogDTO(
				entity.getId(),
				entity.getPosition(),
				entity.getDateOpen(),
				entity.getDateClose(),
				BrokerMapper.toDTO(entity.getBroker()),
				MarketMapper.toDTO(entity.getMarket()),
				TickerMapper.toDTO(entity.getTicker()),
				CurrencyMapper.toDTO(entity.getCurrency()),
				entity.getItemNumber(),
				entity.getPriceOpen(),
				entity.getPriceClose(),
				entity.getVolume(),
				entity.getVolumeToDeposit(),
				entity.getStopLoss(),
				entity.getTakeProfit(),
				entity.getOutcomeExpected(),
				entity.getRisk(),
				entity.getFees(),
				entity.getOutcome(),
				entity.getOutcomePercent(),
				entity.getProfit(),
				entity.getNote(),
				null,
				entity.getGrade(),
				entity.getGoal());
	}
}
