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

		throw new RuntimeException("Not implemented");

		//return e;
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
