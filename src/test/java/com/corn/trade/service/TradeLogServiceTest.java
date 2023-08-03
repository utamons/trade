package com.corn.trade.service;

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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TradeLogServiceTest {

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
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		tradeServiceSpy = spy(tradeService);
	}

	@Test
	public void testOpen_LongPosition() {
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
				/* Set necessary parameters for the buy method */
				openDTO.itemBought(), openDTO.totalBought(), openDTO.openCommission(), openDTO.dateOpen(),
				broker, currency, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateOpen(openDTO, true);
	}

	@Test
	public void testOpen_ShortPosition() {
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
				/* Set necessary parameters for the buy method */
				openDTO.itemSold(), openDTO.totalSold(), openDTO.openCommission(), openDTO.dateOpen(),
				broker, currency, tradeLog
		);

		verify(tradeServiceSpy, times(1)).validateOpen(openDTO, false);
	}

	@Test
	public void testValidateOpen_ValidLongPosition() {
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
	public void testValidateOpen_InvalidLongPosition_TotalBoughtNull() {
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
	public void testValidateOpen_InvalidLongPosition_ItemBoughtNull() {
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
	public void testValidateOpen_InvalidLongPosition_TotalBoughtZero() {
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
	public void testValidateOpen_ValidShortPosition() {
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
	public void testValidateOpen_InvalidShortPosition_TotalSoldNull() {
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
	public void testValidateOpen_InvalidShortPosition_ItemSoldNull() {
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
	public void testValidateOpen_InvalidShortPosition_TotalSoldZero() {
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


}
