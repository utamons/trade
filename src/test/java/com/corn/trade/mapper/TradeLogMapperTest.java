package com.corn.trade.mapper;

import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.entity.*;
import com.corn.trade.service.CurrencyRateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.corn.trade.util.Util.round;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeLogMapperTest {
	@Test
	void testToOpen_LongPosition() {
		// Arrange
		Broker broker = mock(Broker.class);
		Market market = mock(Market.class);
		Ticker ticker = mock(Ticker.class);
		// TradeLog initialisation
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long", // position
				LocalDateTime.of(2021, 1, 1, 0, 0), // dateOpen
				null, // brokerId
				null, // marketId
				null, // tickerId
				100.0, // estimatedPriceOpen
				5.0, // estimatedFees
				95.0, // estimatedBreakEven
				95L, // estimatedItems
				195.0, // riskToCapitalPc
				955.0, // risk
				95.0, // levelPrice
				925.0, // atr
				90.0, // openStopLoss
				80.0, // openTakeProfit
				null, // itemBought
				null, // itemSold
				null, // totalBought
				null, // totalSold
				null, // openCommission
				"Long trade" // note
		);

		// Act
		TradeLog tradeLog = TradeLogMapper.toOpen(openDTO, broker, market, ticker);

		// Assert
		assertEquals(broker, tradeLog.getBroker());
		assertEquals(market, tradeLog.getMarket());
		assertEquals(ticker, tradeLog.getTicker());
		assertEquals("long", tradeLog.getPosition());
		assertEquals(100.0, tradeLog.getEstimatedPriceOpen());
		assertEquals(5.0, tradeLog.getEstimatedFees());
		assertEquals(95.0, tradeLog.getEstimatedBreakEven());
		assertEquals(95L, tradeLog.getEstimatedItems());
		assertEquals(195.0, tradeLog.getRiskToCapitalPc());
		assertEquals(955.0, tradeLog.getRisk());
		assertEquals(95.0, tradeLog.getLevelPrice());
		assertEquals(925.0, tradeLog.getAtr());
		assertEquals(90.0, tradeLog.getOpenStopLoss());
		assertEquals(80.0, tradeLog.getOpenTakeProfit());
		assertEquals(0, tradeLog.getPartsClosed());
		assertEquals("Long trade", tradeLog.getNote());
	}

	@Test
	void testToDTO() throws JsonProcessingException {
		// Arrange
		TradeLog entity   = mock(TradeLog.class);
		Ticker  ticker   = mock(Ticker.class);
		Currency currency = mock(Currency.class);
		Broker   broker   = mock(Broker.class);
		Currency brokerCurrency = mock(Currency.class);
		CurrencyRateService currencyRateService = mock(CurrencyRateService.class);

		// Set up the behavior of the entity mock for long position
		when(ticker.getCurrency()).thenReturn(currency);
		when(entity.getPosition()).thenReturn("long");
		when(entity.isLong()).thenReturn(true);
		when(entity.getOpenCommission()).thenReturn(10.0);
		when(entity.getCloseCommission()).thenReturn(5.0);
		when(entity.getBrokerInterest()).thenReturn(3.0);
		when(entity.getCurrency()).thenReturn(currency);
		when(entity.getBroker()).thenReturn(broker);
		when(broker.getFeeCurrency()).thenReturn(brokerCurrency);
		when(entity.getDateOpen()).thenReturn(LocalDateTime.now());
		when(entity.getDateClose()).thenReturn(LocalDateTime.now());
		when(entity.getMarket()).thenReturn(mock(Market.class));
		when(entity.getTicker()).thenReturn(ticker);
		when(entity.getEstimatedPriceOpen()).thenReturn(100.0);
		when(entity.getEstimatedFees()).thenReturn(5.0);
		when(entity.getEstimatedBreakEven()).thenReturn(95.0);
		when(entity.getEstimatedItems()).thenReturn(10L);
		when(entity.getRiskToCapitalPc()).thenReturn(2.5);
		when(entity.getRisk()).thenReturn(0.5);
		when(entity.getLevelPrice()).thenReturn(98.0);
		when(entity.getAtr()).thenReturn(1.2);
		when(entity.getOpenStopLoss()).thenReturn(90.0);
		when(entity.getOpenTakeProfit()).thenReturn(105.0);
		when(entity.getItemBought()).thenReturn(20L);
		when(entity.getItemSold()).thenReturn(15L);
		when(entity.getTotalBought()).thenReturn(200.0);
		when(entity.getTotalSold()).thenReturn(180.0);
		when(entity.getFinalStopLoss()).thenReturn(87.0);
		when(entity.getFinalTakeProfit()).thenReturn(108.0);
		when(entity.getPartsClosed()).thenReturn(2L);
		when(entity.getNote()).thenReturn("Long trade");
		when(currencyRateService.convert(any(), any(), any(), any())).thenReturn(100.0);
		when(broker.getFeeCurrency()).thenReturn(currency);
		when(currency.getName()).thenReturn("USD");

		// Act
		TradeLogDTO dto = new TradeLogMapper(currencyRateService).toDTO(entity);

		// Assert
		assertEquals("long", dto.position());
		assertEquals(entity.getDateOpen(), dto.dateOpen());
		assertEquals(entity.getDateClose(), dto.dateClose());
		assertEquals(round(100.00), dto.estimatedPriceOpen());
		assertEquals(round(5.00), dto.estimatedFees());
		assertEquals(round(95.00), dto.estimatedBreakEven());
		assertEquals(10L, dto.estimatedItems());
		assertEquals(round(2.5), dto.riskToCapitalPc());
		assertEquals(round(0.5), dto.risk());
		assertEquals(round(98.00), dto.levelPrice());
		assertEquals(round(1.2), dto.atr());
		assertEquals(round(90.00), dto.openStopLoss());
		assertEquals(round(105.00), dto.openTakeProfit());
		assertEquals(20L, dto.itemBought());
		assertEquals(15L, dto.itemSold());
		assertEquals(round(200.00), dto.totalBought());
		assertEquals(round(180.00), dto.totalSold());
		assertEquals(round(87.00), dto.finalStopLoss());
		assertEquals(round(108.00), dto.finalTakeProfit());
		assertEquals(round(10.00), dto.openCommission());
		assertEquals(round(5.00), dto.closeCommission());
		assertEquals(2L, dto.partsClosed());
		assertEquals("Long trade", dto.note());
	}

}
