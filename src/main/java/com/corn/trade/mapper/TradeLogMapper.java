package com.corn.trade.mapper;

import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.entity.*;
import com.corn.trade.service.CurrencyRateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.corn.trade.util.Util.round;

@Service
public class TradeLogMapper {
	public static final Logger              logger = LoggerFactory.getLogger(TradeLogMapper.class);
	private final CurrencyRateService currencyRateService;

	public TradeLogMapper(CurrencyRateService currencyRateService) {
		this.currencyRateService = currencyRateService;
	}

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

	public TradeLogDTO toDTO(TradeLog entity) throws JsonProcessingException {
		double   openCommission  = entity.getOpenCommission() == null ? 0 : entity.getOpenCommission();
		double   closeCommission = entity.getCloseCommission() == null ? 0 : entity.getCloseCommission();
		double   brokerInterest  = entity.getBrokerInterest() == null ? 0 : entity.getBrokerInterest();
		Currency currency        = entity.getCurrency();
		Broker   broker          = entity.getBroker();
		Currency brokerCurrency  = broker.getFeeCurrency();

		double totalFees = openCommission + closeCommission + brokerInterest;
		double totalFeesConverted =
				currencyRateService.convert(brokerCurrency, currency, totalFees, entity.getDateClose().toLocalDate());

		double outcome = entity.isLong() ? entity.getTotalSold() - entity.getTotalBought() :
		                 entity.getTotalBought() - entity.getTotalSold();
		outcome = outcome - totalFeesConverted;

		double outcomePc = (entity.isLong() ? outcome / entity.getTotalBought() :
		                   outcome / entity.getTotalSold()) * 100;

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
				round(entity.getEstimatedPriceOpen()),
				round(entity.getEstimatedFees()),
				round(entity.getEstimatedBreakEven()),
				entity.getEstimatedItems(),
				round(entity.getRiskToCapitalPc()),
				round(entity.getRisk()),
				round(entity.getLevelPrice()),
				round(entity.getAtr()),
				//------------------------------
				round(entity.getOpenStopLoss()),
				round(entity.getOpenTakeProfit()),
				round(brokerInterest),
				round(entity.getTotalBought()),
				round(entity.getTotalSold()),
				entity.getItemBought(),
				entity.getItemSold(),
				round(entity.getFinalStopLoss()),
				round(entity.getFinalTakeProfit()),
				round(openCommission),
				round(closeCommission),
				entity.getNote(),
				//------------------------------
				round(outcome),
				round(outcomePc));
	}

}
