package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.TradeLogDTO;
import com.corn.trade.web.dto.TradeLogOpenDTO;
import com.corn.trade.web.entity.*;
import com.corn.trade.web.service.CurrencyRateService;
import com.corn.trade.web.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TradeLogMapper {
	public static final Logger              logger = LoggerFactory.getLogger(TradeLogMapper.class);
	private final       CurrencyRateService currencyRateService;

	public TradeLogMapper(CurrencyRateService currencyRateService) {
		this.currencyRateService = currencyRateService;
	}

	public static TradeLog toOpen(TradeLogOpenDTO open,
	                              Broker broker,
	                              Market market,
	                              Ticker ticker,
	                              Currency currency) {

		TradeLog e = new TradeLog();

		// Basic data
		e.setBroker(broker);
		e.setMarket(market);
		e.setTicker(ticker);
		e.setPosition(open.position());
		e.setCurrency(currency);

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
		e.setOpenCommission(open.openCommission());
		e.setDateOpen(open.dateOpen());
		e.setOpenStopLoss(open.openStopLoss());
		e.setOpenTakeProfit(open.openTakeProfit());
		e.setPartsClosed(0L);
		e.setNote(open.note());
		e.setItemBought(open.itemBought());
		e.setItemSold(open.itemSold());
		e.setTotalBought(open.totalBought());
		e.setTotalSold(open.totalSold());

		return e;
	}

	public TradeLogDTO toDTO(TradeLog entity) throws JsonProcessingException {
		double   openCommission  = entity.getOpenCommission() == null ? 0 : entity.getOpenCommission();
		double   closeCommission = entity.getCloseCommission() == null ? 0 : entity.getCloseCommission();
		double   brokerInterest  = entity.getBrokerInterest() == null ? 0 : entity.getBrokerInterest();
		Currency currency        = entity.getCurrency();
		Broker   broker          = entity.getBroker();
		Currency brokerCurrency  = broker.getFeeCurrency();
		Double outcome = null, outcomePc = null;

		if (entity.getTotalSold() != null && entity.getTotalBought() != null) {
			LocalDate dateClose = entity.getDateClose() == null ? LocalDate.now() : entity.getDateClose().toLocalDate();
			double totalFees = openCommission + closeCommission + brokerInterest;
			double totalFeesConverted =
					currencyRateService.convert(brokerCurrency, currency, totalFees, dateClose);

			outcome = entity.getTotalSold() - entity.getTotalBought();
			outcome = outcome - totalFeesConverted;

			outcomePc = (entity.isLong() ? outcome / entity.getTotalBought() :
					outcome / entity.getTotalSold()) * 100;
		}

		return new TradeLogDTO(
				entity.getId(),
				entity.getPosition(),
				entity.getDateOpen(),
				entity.getDateClose(),
				BrokerMapper.toDTO(broker),
				MarketMapper.toDTO(entity.getMarket()),
				TickerMapper.toDTO(entity.getTicker()),
				CurrencyMapper.toDTO(currency),
				//-----------------------------
				Util.round(entity.getEstimatedPriceOpen()),
				Util.round(entity.getEstimatedFees()),
				Util.round(entity.getEstimatedBreakEven()),
				entity.getEstimatedItems(),
				Util.round(entity.getRiskToCapitalPc()),
				Util.round(entity.getRisk()),
				Util.round(entity.getLevelPrice()),
				Util.round(entity.getAtr()),
				//------------------------------
				Util.round(entity.getOpenStopLoss()),
				Util.round(entity.getOpenTakeProfit()),
				Util.round(brokerInterest),
				Util.round(entity.getTotalBought()),
				Util.round(entity.getTotalSold()),
				entity.getItemBought(),
				entity.getItemSold(),
				Util.round(entity.getFinalStopLoss()),
				Util.round(entity.getFinalTakeProfit()),
				Util.round(openCommission),
				Util.round(closeCommission),
				entity.getPartsClosed(),
				entity.getNote(),
				//------------------------------
				Util.round(outcome),
				Util.round(outcomePc));
	}

}
