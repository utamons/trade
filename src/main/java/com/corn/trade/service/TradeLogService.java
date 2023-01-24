package com.corn.trade.service;

import com.corn.trade.dto.*;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.TradeLogMapper;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.MarketRepository;
import com.corn.trade.repository.TickerRepository;
import com.corn.trade.repository.TradeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

	public void close(TradeLogCloseDTO closeDTO) throws JsonProcessingException {
		TradeLog open = tradeLogRepo.getReferenceById(closeDTO.getId());
		open.setDateClose(closeDTO.getDateClose());
		open.setPriceClose(closeDTO.getPriceClose());

		BigDecimal outcome = open.getPriceClose()
		                         .subtract(open.getPriceOpen())
				.multiply(BigDecimal.valueOf(open.getItemNumber()));
		BigDecimal outcomePercent = outcome.divide(open.getPriceOpen(), 12, RoundingMode.HALF_EVEN)
				                                   .multiply(BigDecimal.valueOf(100.00));

		if (!closeDTO.getFees().equals(BigDecimal.ZERO))
			cashService.fee(closeDTO.getFees(),  open.getBroker(), open);

		BigDecimal openAmount = open.getPriceOpen().multiply(BigDecimal.valueOf(open.getItemNumber()));

		BigDecimal percentToCapital = cashService.percentToCapital(outcome, openAmount, open.getCurrency());

		open.setOutcome(outcome);
		open.setOutcomePercent(outcomePercent);
		open.setProfit(percentToCapital);
		open.setFees(open.getFees().add(closeDTO.getFees()));
		open.setGrade(closeDTO.getGrade());
		if (closeDTO.getNote() != null)
			open.setNote(closeDTO.getNote());

		BigDecimal closeAmount = open.getPriceClose().multiply(BigDecimal.valueOf(open.getItemNumber()));

		open = tradeLogRepo.save(open);

		cashService.sell(open.getVolume(), closeAmount, open.getBroker(), open.getCurrency(), open);
	}

	public Page<TradeLogDTO> getPage(TradeLogPageReqDTO pageReqDTO) {
		Pageable pageable = PageRequest.of(pageReqDTO.getPageNumber(), pageReqDTO.getPageSize(),
		                                   Sort.by("dateOpen").descending());
		Page<TradeLog> page = tradeLogRepo.findAll(pageable);
		return page.map(TradeLogMapper::toDTO);
	}
}
