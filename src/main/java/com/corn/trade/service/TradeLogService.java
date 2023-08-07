package com.corn.trade.service;

import com.corn.trade.dto.TradeLogCloseDTO;
import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.dto.TradeLogPageReqDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.Market;
import com.corn.trade.entity.Ticker;
import com.corn.trade.entity.TradeLog;
import com.corn.trade.mapper.TradeLogMapper;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.MarketRepository;
import com.corn.trade.repository.TickerRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@Transactional
public class TradeLogService {

	private static final Logger logger = LoggerFactory.getLogger(TradeLogService.class);

	private final TradeLogRepository tradeLogRepo;
	private final BrokerRepository   brokerRepo;
	private final MarketRepository   marketRepo;
	private final TickerRepository   tickerRepo;
	private final CashService        cashService;

	private final TradeLogMapper tradeLogMapper;

	public TradeLogService(TradeLogRepository tradeLogRepo,
	                       BrokerRepository brokerRepo,
	                       MarketRepository marketRepo,
	                       TickerRepository tickerRepo,
	                       CashService cashService, TradeLogMapper tradeLogMapper) {
		this.tradeLogRepo = tradeLogRepo;
		this.brokerRepo = brokerRepo;
		this.marketRepo = marketRepo;
		this.tickerRepo = tickerRepo;
		this.cashService = cashService;
		this.tradeLogMapper = tradeLogMapper;
	}


	/**
	 * Opening a position
	 *
	 * @param openDTO DTO containing the data to open a position
	 */
	public void open(TradeLogOpenDTO openDTO) {
		Broker broker = brokerRepo.getReferenceById(openDTO.brokerId());
		Market market = marketRepo.getReferenceById(openDTO.marketId());
		Ticker ticker = tickerRepo.getReferenceById(openDTO.tickerId());

		boolean isLong = openDTO.position().equals("long");

		validateOpen(openDTO, isLong);

		TradeLog tradeLog = TradeLogMapper.toOpen(openDTO, broker, market, ticker);
		tradeLog = tradeLogRepo.save(tradeLog);

		if (isLong)
			cashService.buy(openDTO.itemBought(), openDTO.totalBought(), openDTO.openCommission(),
			                openDTO.dateOpen(), broker, ticker.getCurrency(), tradeLog);
		else
			cashService.sellShort(openDTO.itemSold(),
			                      openDTO.totalSold(),
			                      openDTO.openCommission(),
			                      openDTO.dateOpen(),
			                      broker,
			                      ticker.getCurrency(),
			                      tradeLog);

		tradeLogRepo.flush();
	}

	public void validateOpen(TradeLogOpenDTO openDTO, boolean isLong) {
		validateOpenLong(openDTO, isLong);
		validateOpenShort(openDTO, isLong);
	}

	private static void validateOpenShort(TradeLogOpenDTO openDTO, boolean isLong) {
		if (!isLong && openDTO.totalSold() == null)
			throw new IllegalArgumentException("Total sold must not be null");
		if (!isLong && openDTO.itemSold() == null)
			throw new IllegalArgumentException("Items sold must not be null");
		if (!isLong && openDTO.totalSold() <= 0)
			throw new IllegalArgumentException("Total sold must be greater than 0");
		if (!isLong && openDTO.itemSold() <= 0)
			throw new IllegalArgumentException("Items sold must be greater than 0");
	}

	private static void validateOpenLong(TradeLogOpenDTO openDTO, boolean isLong) {
		if (isLong && openDTO.totalBought() == null)
			throw new IllegalArgumentException("Total bought must not be null");
		if (isLong && openDTO.itemBought() == null)
			throw new IllegalArgumentException("Items bought must not be null");
		if (isLong && openDTO.totalBought() <= 0)
			throw new IllegalArgumentException("Total bought must be greater than 0");
		if (isLong && openDTO.itemBought() <= 0)
			throw new IllegalArgumentException("Items bought must be greater than 0");
	}

	public void validateClose(TradeLogCloseDTO closeDTO, boolean isLong) {
		validateCloseLong(closeDTO, isLong);
		validateCloseShort(closeDTO, isLong);
	}

	private static void validateCloseShort(TradeLogCloseDTO closeDTO, boolean isLong) {
		if (!isLong && closeDTO.totalBought() == null)
			throw new IllegalArgumentException("Total bought must not be null");
		if (!isLong && closeDTO.itemBought() == null)
			throw new IllegalArgumentException("Items bought must not be null");
		if (!isLong && closeDTO.totalBought() <= 0)
			throw new IllegalArgumentException("Total bought must be greater than 0");
		if (!isLong && closeDTO.itemBought() <= 0)
			throw new IllegalArgumentException("Items bought must be greater than 0");
	}

	private static void validateCloseLong(TradeLogCloseDTO closeDTO, boolean isLong) {
		if (isLong && closeDTO.totalSold() == null)
			throw new IllegalArgumentException("Total sold must not be null");
		if (isLong && closeDTO.itemSold() == null)
			throw new IllegalArgumentException("Items sold must not be null");
		if (isLong && closeDTO.totalSold() <= 0)
			throw new IllegalArgumentException("Total sold must be greater than 0");
		if (isLong && closeDTO.itemSold() <= 0)
			throw new IllegalArgumentException("Items sold must be greater than 0");
	}

	/**
	 * Closing a position
	 *
	 * @param closeDTO DTO containing the data to close a position
	 */
	public void close(TradeLogCloseDTO closeDTO) {
		TradeLog open   = tradeLogRepo.getReferenceById(closeDTO.id());
		boolean  isLong = open.getPosition().equals("long");

		validateClose(closeDTO, isLong);

		final Broker broker = open.getBroker();

		if (isLong)
			cashService.sell(closeDTO.itemSold(),
			                 closeDTO.totalSold(),
			                 closeDTO.closeCommission(),
			                 closeDTO.dateClose(),
			                 broker,
			                 open);
		else
			cashService.buyShort(closeDTO.itemBought(),
			                     closeDTO.totalBought(),
			                     closeDTO.closeCommission(),
			                     closeDTO.brokerInterest(),
			                     closeDTO.dateClose(),
			                     broker,
			                     open);


		if (closeDTO.note() != null)
			open.setNote(closeDTO.note());

		if (open.getDateClose() != null) {
			open.setFinalStopLoss(closeDTO.finalStopLoss());
			open.setFinalTakeProfit(closeDTO.finalTakeProfit());
		}

		tradeLogRepo.save(open);
		tradeLogRepo.flush();
	}

	public Page<TradeLogDTO> getPage(TradeLogPageReqDTO pageReqDTO) {
		Pageable pageable = PageRequest.of(pageReqDTO.getPageNumber(), pageReqDTO.getPageSize(),
		                                   Sort.by("dateOpen").descending());
		Page<TradeLog> page = tradeLogRepo.findAll(pageable);
		return page.map(getMappingTradeLogToTradeLogDTO());
	}

	private Function<TradeLog, TradeLogDTO> getMappingTradeLogToTradeLogDTO() {
		return log -> {
			try {
				return tradeLogMapper.toDTO(log);
			} catch (JsonProcessingException e) {
				logger.error("Error mapping TradeLog to TradeLogDTO", e);
			}
			return null;
		};
	}

	public List<TradeLogDTO> getAllClosedByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.findAllClosedByBroker(broker).stream().map(
				getMappingTradeLogToTradeLogDTO()
		).toList();
	}

	public long getOpenCountByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.opensCountByBroker(broker);
	}

	public List<TradeLogDTO> getAllClosed() {
		return tradeLogRepo.findAllClosed().stream().map(getMappingTradeLogToTradeLogDTO()).toList();
	}
}
