package com.corn.trade.service;

import com.corn.trade.dto.TradeLogCloseDTO;
import com.corn.trade.dto.TradeLogOpenDTO;
import com.corn.trade.dto.TradeLogPageDTO;
import com.corn.trade.dto.TradeLogPageReqDTO;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.TradeLogMapper;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.MarketRepository;
import com.corn.trade.repository.TickerRepository;
import com.corn.trade.repository.TradeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class TradeLogService {

	private final TradeLogRepository tradeLogRepo;
	private final BrokerRepository brokerRepo;
	private final MarketRepository marketRepo;
	private final TickerRepository tickerRepo;
	private final CashService cashService;

	public TradeLogService(TradeLogRepository tradeLogRepo,
	                       BrokerRepository brokerRepo,
	                       MarketRepository marketRepo,
	                       TickerRepository tickerRepo,
	                       CashService cashService) {
		this.tradeLogRepo = tradeLogRepo;
		this.brokerRepo = brokerRepo;
		this.marketRepo = marketRepo;
		this.tickerRepo = tickerRepo;
		this.cashService = cashService;
	}


	public void open(TradeLogOpenDTO openDTO) {
		Broker broker = brokerRepo.getReferenceById(openDTO.getBrokerId());
		Market market = marketRepo.getReferenceById(openDTO.getMarketId());
		Ticker ticker = tickerRepo.getReferenceById(openDTO.getTickerId());
		BigDecimal depositAmount = cashService.lastDepositAmount(broker,ticker.getCurrency());
		TradeLog tradeLog = TradeLogMapper.toEntity(openDTO,broker,market,ticker,depositAmount);

		tradeLog = tradeLogRepo.save(tradeLog);
		cashService.buy(tradeLog.getVolume(), broker, ticker.getCurrency(), tradeLog);
		if (!tradeLog.getFees().equals(BigDecimal.ZERO))
			cashService.fee(tradeLog.getFees(), broker, tradeLog);
	}

	public void close(TradeLogCloseDTO closeDTO) {

	}

	public TradeLogPageDTO page(TradeLogPageReqDTO pageReqDTO) {
		return null;
	}
}
