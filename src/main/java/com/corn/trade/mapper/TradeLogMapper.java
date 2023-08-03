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

	public static TradeLog toOpen(TradeLogOpenDTO open,
	                              Broker broker,
	                              Market market,
	                              Ticker ticker) {

		TradeLog e = new TradeLog();

		// Basic data
		e.setBroker(broker);
		e.setMarket(market);
		e.setTicker(ticker);
		e.setPosition(open.position());

		// Estimated data
		e.setEstimatedPriceOpen(open.estimatedPriceOpen());
		e.setEstimatedFees(open.estimatedFees());
		e.setEstimatedBreakEven(open.estimatedBreakEven());
		e.setEstimatedItems(open.estimatedItems());
		e.setRiskToCapitalPc(open.riskToCapitalPc());
		e.setRisk(open.risk());
		e.setLevelPrice(open.levelPrice());
		e.setAtr(open.atr());

		// Actual data
		e.setOpenStopLoss(open.openStopLoss());
		e.setOpenTakeProfit(open.openTakeProfit());
		e.setNote(open.note());

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
		throw new RuntimeException("Not implemented");
	}
}
