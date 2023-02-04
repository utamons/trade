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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TradeLogService {

	private final TradeLogRepository tradeLogRepo;
	private final BrokerRepository   brokerRepo;
	private final MarketRepository   marketRepo;
	private final TickerRepository   tickerRepo;
	private final CashService        cashService;

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
		Broker   broker        = brokerRepo.getReferenceById(openDTO.getBrokerId());
		Market   market        = marketRepo.getReferenceById(openDTO.getMarketId());
		Ticker   ticker        = tickerRepo.getReferenceById(openDTO.getTickerId());
		Double   depositAmount = cashService.lastDepositAmount(broker, ticker.getCurrency());
		TradeLog tradeLog      = TradeLogMapper.toEntity(openDTO, broker, market, ticker, depositAmount);

		tradeLog = tradeLogRepo.save(tradeLog);
		cashService.buy(tradeLog.getVolume(), broker, ticker.getCurrency(), tradeLog);
		if (tradeLog.getFees() != 0.0)
			cashService.fee(tradeLog.getFees(), broker, tradeLog);
	}

	public void close(TradeLogCloseDTO closeDTO) throws JsonProcessingException {
		TradeLog open = tradeLogRepo.getReferenceById(closeDTO.getId());
		open.setDateClose(closeDTO.getDateClose());
		open.setPriceClose(closeDTO.getPriceClose());

		long items = open.getItemNumber();

		double outcome = (open.getPriceClose() - open.getPriceOpen()) * items;

		double sum = closeDTO.getPriceClose() * items;

		double outcomePercent = outcome / open.getVolume() * 100.0;

		double fees = cashService.getFees(open.getBroker(), open.getTicker(), items, sum, closeDTO.getDateClose().toLocalDate());

		if (fees != 0)
			cashService.fee(fees, open.getBroker(), open);

		open.setFees(open.getFees() + fees);

		double openAmount = open.getVolume();

		double percentToCapital = cashService.percentToCapital(outcome, openAmount, open.getCurrency());

		open.setOutcome(outcome);
		open.setOutcomePercent(outcomePercent);
		open.setProfit(percentToCapital);

		if (closeDTO.getNote() != null)
			open.setNote(closeDTO.getNote());

		double closeAmount = open.getPriceClose() * open.getItemNumber();

		open = tradeLogRepo.save(open);

		cashService.sell(open.getVolume(), closeAmount, open.getBroker(), open.getCurrency(), open);
	}

	public Page<TradeLogDTO> getPage(TradeLogPageReqDTO pageReqDTO) {
		Pageable pageable = PageRequest.of(pageReqDTO.getPageNumber(), pageReqDTO.getPageSize(),
		                                   Sort.by("dateOpen").descending());
		Page<TradeLog> page = tradeLogRepo.findAll(pageable);
		return page.map(TradeLogMapper::toDTO);
	}

	public List<TradeLogDTO> getAllClosedByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.findAllClosedByBroker(broker).stream().map(TradeLogMapper::toDTO).collect(Collectors.toList());
	}

	public long getOpenCountByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.opensByBroker(broker);
	}

	public List<TradeLogDTO> getAllClosed() {
		return tradeLogRepo.findAllClosed().stream().map(TradeLogMapper::toDTO).collect(Collectors.toList());
	}
}
