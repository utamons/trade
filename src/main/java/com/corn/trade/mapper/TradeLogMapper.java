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
		TradeLog e = new TradeLog();
		e.setPosition(open.getPosition());
		e.setDateOpen(open.getDateOpen());
		e.setBroker(broker);
		e.setMarket(market);
		e.setTicker(ticker);
		e.setCurrency(ticker.getCurrency());
		e.setItemNumber(open.getItemNumber());
		e.setPriceOpen(open.getPriceOpen());
		e.setBreakEven(open.getBreakEven());

		Double volume = open.getPriceOpen()*open.getItemNumber();
		Double volumeToDeposit = volume/depositAmount*100.0;

		e.setVolume(volume);
		e.setVolumeToDeposit(volumeToDeposit);
		e.setStopLoss(open.getStopLoss());
		e.setTakeProfit(open.getTakeProfit());
		e.setOutcomeExpected(open.getOutcomeExpected());
		e.setRisk(open.getRisk());
		e.setFees(open.getFees() == null ? 0.0 : open.getFees());
		e.setNote(open.getNote());
		e.setGoal(open.getGoal());

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
				entity.getPriceOpen(),
				entity.getPriceClose(),
				entity.getVolume(),
				entity.getVolumeToDeposit(),
				entity.getStopLoss(),
				entity.getTakeProfit(),
				entity.getOutcomeExpected(),
				entity.getRisk(),
				entity.getBreakEven(),
				entity.getFees(),
				outcome,
				entity.getOutcomePercent(),
				entity.getProfit(),
				entity.getNote(),
				null,
				entity.getGrade(),
				entity.getGoal(),
				entity.getBrokerInterest(),
				parentId);
	}
}
