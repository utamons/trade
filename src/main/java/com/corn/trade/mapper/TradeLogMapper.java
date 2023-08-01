package com.corn.trade.mapper;

import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.Market;
import com.corn.trade.entity.Ticker;
import com.corn.trade.entity.TradeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeLogMapper {
	public static Logger logger = LoggerFactory.getLogger(TradeLogMapper.class);

	public static TradeLog toEntity(TradeLogOpenDTO open,
	                                Broker broker,
	                                Market market,
	                                Ticker ticker,
	                                Double depositAmount) {
		double realVolume = open.position().equals("long") ? open.totalBought() : open.totalSold();
		TradeLog e = new TradeLog();
		e.setPosition(open.position());
		e.setDateOpen(open.dateOpen());
		e.setBroker(broker);
		e.setMarket(market);
		e.setTicker(ticker);
		e.setCurrency(ticker.getCurrency());
		e.setItemNumber(open.itemNumber());
		e.setEstimatedPriceOpen(open.priceOpen());
		e.setEstimatedBreakEven(open.breakEven());

		Double volume = open.priceOpen() * open.itemNumber();
		Double volumeToDeposit = realVolume/depositAmount*100.0;

		e.setVolume(volume);
		e.setVolumeToDeposit(volumeToDeposit);
		e.setOpenStopLoss(open.stopLoss());
		e.setOpenTakeProfit(open.takeProfit());
		e.setOutcomeExpected(open.outcomeExpected());
		e.setRisk(open.risk());
		e.setFees(open.fees() == null ? 0.0 : open.fees());
		e.setNote(open.note());
		e.setGoal(open.goal());
		e.setAtr(open.atr());
		e.setLevelPrice(open.levelPrice());
		e.setTotalBought(open.totalBought());
		e.setTotalSold(open.totalSold());

		return e;
	}

	public static Double roundZeroOutcome(Double outcome) {
		if (outcome == null)
			return null;
		if (outcome <= 0.01 && outcome > 0)
			return 0.0;
		if (outcome >= -0.01 && outcome < 0)
			return 0.0;
		return outcome;
	}

	public static TradeLogDTO toDTO(TradeLog entity) {
		Double outcome = roundZeroOutcome(entity.getOutcome());
		Long parentId = entity.getParent() == null ? null : entity.getParent().getId();
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
				entity.getEstimatedPriceOpen(),
				entity.getAveragePriceClose(),
				entity.getVolume(),
				entity.getVolumeToDeposit(),
				entity.getOpenStopLoss(),
				entity.getOpenTakeProfit(),
				entity.getOutcomeExpected(),
				entity.getRisk(),
				entity.getEstimatedBreakEven(),
				entity.getFees(),
				outcome,
				entity.getOutcomePercent(),
				entity.getProfit(),
				entity.getNote(),
				null,
				entity.getGrade(),
				entity.getGoal(),
				entity.getBrokerInterest(),
				parentId,
				entity.getLevelPrice(),
				entity.getAtr(),
				entity.getTotalBought(),
				entity.getTotalSold()
		);
	}
}
