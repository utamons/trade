package com.corn.trade.service;

import com.corn.trade.dto.TradeLogCloseDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.TradeLogMapper;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.MarketRepository;
import com.corn.trade.repository.TickerRepository;
import com.corn.trade.repository.TradeLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeLogServiceTest {

	@Mock
	private BrokerRepository brokerRepo;

	@Mock
	private MarketRepository marketRepo;

	@Mock
	private TickerRepository tickerRepo;

	@Mock
	private TradeLogRepository tradeLogRepo;

	@Mock
	private CashService cashService;

	@InjectMocks
	private TradeLogService tradeService;

	private TradeLogService tradeServiceSpy;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		tradeServiceSpy = spy(tradeService);
	}

	@Test
	void testOpen_LongPosition() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				99L,
				null,
				9800.0,
				null,
				2.1,
				"TestComment"
		);

		// Set other necessary fields in the openDTO
		Currency currency = new Currency("TestCurrency");
		Broker broker = new Broker("TestBroker", currency);
		Market market = new Market("TestMarket");
		Ticker ticker = new Ticker("TestTicker", "TestTickerCode", currency);

		TradeLog tradeLog = TradeLogMapper.toOpen(openDTO, broker, market, ticker);

		when(tradeLogRepo.save(any())).thenReturn(tradeLog);
		when(brokerRepo.getReferenceById(openDTO.brokerId())).thenReturn(broker);
		when(marketRepo.getReferenceById(openDTO.marketId())).thenReturn(market);
		when(tickerRepo.getReferenceById(openDTO.tickerId())).thenReturn(ticker);


		// Act
		tradeServiceSpy.open(openDTO);

		// Assert
		verify(cashService, times(1)).buy(
				openDTO.itemBought(), openDTO.totalBought(), openDTO.openCommission(), openDTO.dateOpen(),
				broker, currency, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateOpen(openDTO, true);
	}

	@Test
	void testOpen_ShortPosition() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"short",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				99L,
				null,
				9800.0,
				2.1,
				"TestComment"
		);

		// Set other necessary fields in the openDTO
		Currency currency = new Currency("TestCurrency");
		Broker broker = new Broker("TestBroker", currency);
		Market market = new Market("TestMarket");
		Ticker ticker = new Ticker("TestTicker", "TestTickerCode", currency);

		TradeLog tradeLog = TradeLogMapper.toOpen(openDTO, broker, market, ticker);

		when(tradeLogRepo.save(any())).thenReturn(tradeLog);
		when(brokerRepo.getReferenceById(openDTO.brokerId())).thenReturn(broker);
		when(marketRepo.getReferenceById(openDTO.marketId())).thenReturn(market);
		when(tickerRepo.getReferenceById(openDTO.tickerId())).thenReturn(ticker);


		// Act
		tradeServiceSpy.open(openDTO);

		// Assert
		verify(cashService, times(1)).sellShort(
				openDTO.itemSold(), openDTO.totalSold(), openDTO.openCommission(), openDTO.dateOpen(),
				broker, currency, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateOpen(openDTO, false);
	}

	@Test
	void testValidateOpen_ValidLongPosition() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				99L,
				null,
				9800.0,
				null,
				2.1,
				"TestComment"
		);

		// Act & Assert (no exception should be thrown)
		tradeService.validateOpen(openDTO, true);
	}

	@Test
	void testValidateOpen_InvalidLongPosition_TotalBoughtNull() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				99L,
				null,
				null,
				null,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, true));
	}

	@Test
	void testValidateOpen_InvalidLongPosition_ItemBoughtNull() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				null,
				9800.0,
				null,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, true));
	}

	@Test
	void testValidateOpen_InvalidLongPosition_TotalBoughtZero() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"long",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				99L,
				null,
				0.0,
				null,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, true));
	}

	@Test
	void testValidateOpen_ValidShortPosition() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"short",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				99L,
				null,
				9800.0,
				2.1,
				"TestComment"
		);

		// Act & Assert (no exception should be thrown)
		tradeService.validateOpen(openDTO, false);
	}

	@Test
	void testValidateOpen_InvalidShortPosition_TotalSoldNull() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"short",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				99L,
				null,
				null,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, false));
	}

	@Test
	void testValidateOpen_InvalidShortPosition_ItemSoldNull() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"short",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				null,
				null,
				9800.0,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, false));
	}

	@Test
	void testValidateOpen_InvalidShortPosition_TotalSoldZero() {
		// Arrange
		TradeLogOpenDTO openDTO = new TradeLogOpenDTO(
				"short",
				LocalDateTime.now(),
				1L,
				1L,
				1L,
				100.0,
				2.0,
				1.0,
				100L,
				0.5,
				10.0,
				99.0,
				10.0,
				98.0,
				1100.0,
				null,
				99L,
				null,
				0.0,
				2.1,
				"TestComment"
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateOpen(openDTO, false));
	}

	@Test
	void testClose_LongPosition() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				null,
				10L,
				null,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Set other necessary fields in the openDTO
		Currency currency = new Currency("TestCurrency");
		Broker broker = new Broker("TestBroker", currency);
		Market market = new Market("TestMarket");
		Ticker ticker = new Ticker("TestTicker", "TestTickerCode", currency);

		TradeLog tradeLog = getTradeLog(broker, ticker, market, currency);
		tradeLog.setDateClose(LocalDateTime.now());

		when(tradeLogRepo.getReferenceById(any())).thenReturn(tradeLog);

		// Act
		tradeServiceSpy.close(closeDTO);

		// Assert
		verify(cashService, times(1)).sell(
				closeDTO.itemSold(), closeDTO.totalSold(), closeDTO.closeCommission(), closeDTO.dateClose(),
				broker, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateClose(closeDTO, true);

		assertEquals("TestComment", tradeLog.getNote());
		assertEquals(closeDTO.finalStopLoss(), tradeLog.getFinalStopLoss());
		assertEquals(closeDTO.finalTakeProfit(), tradeLog.getFinalTakeProfit());
	}

	@Test
	void testClose_ShortPosition() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				null,
				1000.0,
				null,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Set other necessary fields in the openDTO
		Currency currency = new Currency("TestCurrency");
		Broker broker = new Broker("TestBroker", currency);
		Market market = new Market("TestMarket");
		Ticker ticker = new Ticker("TestTicker", "TestTickerCode", currency);

		TradeLog tradeLog = getTradeLog(broker, ticker, market, currency);
		tradeLog.setPosition("short");

		when(tradeLogRepo.getReferenceById(any())).thenReturn(tradeLog);

		// Act
		tradeServiceSpy.close(closeDTO);

		// Assert
		verify(cashService, times(1)).buyShort(
				closeDTO.itemBought(), closeDTO.totalBought(), closeDTO.closeCommission(), closeDTO.brokerInterest(),
				closeDTO.dateClose(), broker, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateClose(closeDTO, false);

		assertEquals("TestComment", tradeLog.getNote());
		assertNull(tradeLog.getFinalStopLoss());
		assertNull(tradeLog.getFinalTakeProfit());
	}

	private TradeLog getTradeLog(Broker broker, Ticker ticker, Market market, Currency currency) {
		TradeLog tradeLog = new TradeLog();
		tradeLog.setPosition("long");
		tradeLog.setDateOpen(LocalDateTime.now());
		tradeLog.setBroker(broker);
		tradeLog.setTicker(ticker);
		tradeLog.setMarket(market);
		tradeLog.setCurrency(currency);
		tradeLog.setItemBought(1L);
		tradeLog.setEstimatedPriceOpen(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setOpenStopLoss(1.0);
		tradeLog.setOpenTakeProfit(1.0);
		tradeLog.setLevelPrice(1.0);
		tradeLog.setAtr(1.0);
		tradeLog.setTotalBought(1.0);
		tradeLog.setOpenCommission(1.0);
		tradeLog.setRiskToCapitalPc(1.0);
		tradeLog.setRisk(1.0);
		tradeLog.setEstimatedFees(1.0);
		tradeLog.setEstimatedBreakEven(1.0);
		tradeLog.setEstimatedItems(1L);
		return tradeLog;
	}

	@Test
	void testValidateClose_InvalidLongPosition_TotalSoldNull() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				100L,
				1000.0,
				null,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, true));
	}

	@Test
	void testValidateClose_InvalidLongPosition_ItemSoldNull() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				null,
				1000.0,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, true));
	}

	@Test
	void testValidateClose_InvalidLongPosition_TotalSoldSoldLessThanZero() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				10L,
				1000.0,
				-1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, true));
	}

	@Test
	void testValidateClose_InvalidLongPosition_ItemSoldSoldLessThanZero() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				-10L,
				1000.0,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, true));
	}

	@Test
	void testValidateClose_InvalidShortPosition_TotalBoughtNull() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				100L,
				null,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, false));
	}

	@Test
	void testValidateClose_InvalidShortPosition_ItemBoughtNull() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				null,
				10L,
				1000.0,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, false));
	}

	@Test
	void testValidateClose_InvalidShortPosition_TotalBoughtLessThanZero() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				10L,
				10L,
				-1000.0,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, false));
	}

	@Test
	void testValidateClose_InvalidShortPosition_ItemBoughtLessThanZero() {
		// Arrange
		TradeLogCloseDTO closeDTO = new TradeLogCloseDTO(
				1L,
				-10L,
				10L,
				1000.0,
				1000.0,
				LocalDateTime.now(),
				"TestComment",
				0.5,
				1.01,
				99.0,
				10.0
		);

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> tradeService.validateClose(closeDTO, false));
	}

}
