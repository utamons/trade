package com.corn.trade.service;

import com.corn.trade.dto.*;
import com.corn.trade.entity.*;
import com.corn.trade.mapper.CurrencyMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	private final CurrencyRateService currencyRateService;

	public TradeLogService(TradeLogRepository tradeLogRepo,
	                       BrokerRepository brokerRepo,
	                       MarketRepository marketRepo,
	                       TickerRepository tickerRepo,
	                       CashService cashService, CurrencyRateService currencyRateService) {
		this.tradeLogRepo = tradeLogRepo;
		this.brokerRepo = brokerRepo;
		this.marketRepo = marketRepo;
		this.tickerRepo = tickerRepo;
		this.cashService = cashService;
		this.currencyRateService = currencyRateService;
	}


	public void open(TradeLogOpenDTO openDTO) {
		Broker   broker        = brokerRepo.getReferenceById(openDTO.brokerId());
		Market   market        = marketRepo.getReferenceById(openDTO.marketId());
		Ticker   ticker        = tickerRepo.getReferenceById(openDTO.tickerId());
		Double   depositAmount = cashService.lastDepositAmount(broker, ticker.getCurrency());
		TradeLog tradeLog      = TradeLogMapper.toEntity(openDTO, broker, market, ticker, depositAmount);

		tradeLog = tradeLogRepo.save(tradeLog);
		tradeLogRepo.flush();
		if (openDTO.position().equals("long"))
			cashService.buy(tradeLog.getTotalBought(), broker, ticker.getCurrency(), tradeLog);
		else
			cashService.sellShort(tradeLog.getTotalSold(), broker, ticker.getCurrency(), tradeLog);
		if (openDTO.fees() != 0.0)
			cashService.fee(openDTO.fees(), broker, tradeLog, tradeLog.getDateOpen());
	}

	public void close(TradeLogCloseDTO closeDTO) throws JsonProcessingException {
		TradeLog open = tradeLogRepo.getReferenceById(closeDTO.id());
		boolean isLong = open.getPosition().equals("long");

		if (closeDTO.quantity() != null && closeDTO.quantity() < open.getItemNumber())
			open = copyPartial(closeDTO, open);

		final Broker   broker   = open.getBroker();
		final Currency currency = open.getCurrency();

		double realOpen = isLong ? open.getTotalBought() : open.getTotalSold();
		double realClose = isLong ? closeDTO.totalSold() : closeDTO.totalBought();
		double realDelta = realClose - realOpen;

		double openFees = open.getFees();
		double closeFees = closeDTO.fees();

		final LocalDateTime dateTimeClose  = closeDTO.dateClose();
		final double        brokerInterest = closeDTO.brokerInterest() == null ? 0.0 : closeDTO.brokerInterest();

		final double closeFeesUSD = currencyRateService.convertToUSD(currency.getId(), closeFees, closeDTO.dateClose().toLocalDate());
		final double brokerInterestUSD = currencyRateService.convertToUSD(currency.getId(), brokerInterest, closeDTO.dateClose().toLocalDate());

		final double outcome        = realDelta - openFees - closeFees - brokerInterest;
		final double outcomePercent = outcome / realOpen * 100.0;

		cashService.fee(closeFeesUSD, broker, open, dateTimeClose);
		if (brokerInterest != 0)
			cashService.fee(brokerInterestUSD, broker, open, dateTimeClose);

		double percentToCapital = cashService.percentToCapital(outcome, realOpen, currency);

		open.setFees(openFees + closeFees);
		open.setPriceClose(closeDTO.priceClose());
		open.setDateClose(dateTimeClose);
		open.setOutcome(outcome);
		open.setOutcomePercent(outcomePercent);
		open.setProfit(percentToCapital);
		if (open.isShort()) {
			open.setBrokerInterest(brokerInterest);
			open.setTotalBought(closeDTO.totalBought());
		} else {
			open.setTotalSold(closeDTO.totalSold());
		}

		if (closeDTO.note() != null)
			open.setNote(closeDTO.note());


		open = tradeLogRepo.save(open);

		if (open.getPosition().equals("long"))
			cashService.sell(open.getTotalBought(), realOpen, broker, currency, open);
		else
			cashService.buyShort(open.getTotalSold(), realOpen, broker, currency, open);
	}

	private TradeLog copyPartial(TradeLogCloseDTO closeDTO, TradeLog open) {
		Double   depositAmount = cashService.lastDepositAmount(open.getBroker(), open.getCurrency());
		Double volume = open.getPriceOpen()*closeDTO.quantity();
		Double volumeToDeposit = volume/depositAmount*100.0;

		TradeLog partial = new TradeLog();
		partial.setPosition(open.getPosition());
		partial.setDateOpen(open.getDateOpen());
		partial.setBroker(open.getBroker());
		partial.setMarket(open.getMarket());
		partial.setTicker(open.getTicker());
		partial.setCurrency(open.getCurrency());
		partial.setItemNumber(closeDTO.quantity().longValue());
		partial.setPriceOpen(open.getPriceOpen());
		partial.setVolume(volume);
		partial.setVolumeToDeposit(volumeToDeposit);
		partial.setStopLoss(open.getStopLoss());
		partial.setTakeProfit(open.getTakeProfit());
		partial.setOutcomeExpected(open.getOutcomeExpected());
		partial.setRisk(open.getRisk());
		partial.setFees(open.getFees());
		partial.setNote(open.getNote());
		partial.setBreakEven(open.getBreakEven());
		partial.setBrokerInterest(open.getBrokerInterest());
		partial.setParent(open);
		partial.setTotalBought(open.getTotalBought());
		partial.setTotalSold(open.getTotalSold());

		partial = tradeLogRepo.save(partial);

		open.setItemNumber(open.getItemNumber() - closeDTO.quantity());
		open.setVolume(open.getVolume() - volume);

		tradeLogRepo.save(open);

		return partial;
	}

	public Page<TradeLogDTO> getPage(TradeLogPageReqDTO pageReqDTO) throws JsonProcessingException {
		Pageable pageable = PageRequest.of(pageReqDTO.getPageNumber(), pageReqDTO.getPageSize(),
		                                   Sort.by("dateOpen").descending());
		Page<TradeLog> page = tradeLogRepo.findAll(pageable);
		for (TradeLog log : page) {
			cashService.applyBorrowInterest(log);
		}
		return page.map(TradeLogMapper::toDTO);
	}

	public List<TradeLogDTO> getAllClosedByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.findAllClosedByBroker(broker).stream().map(TradeLogMapper::toDTO).collect(Collectors.toList());
	}

	public long getOpenCountByBroker(Long brokerId) {
		Broker broker = brokerRepo.getReferenceById(brokerId);
		return tradeLogRepo.opensCountByBroker(broker);
	}

	public List<TradeLogDTO> getAllClosed() {
		return tradeLogRepo.findAllClosed().stream().map(TradeLogMapper::toDTO).collect(Collectors.toList());
	}

	public void update(TradeLogOpenDTO openDTO) throws JsonProcessingException {
		TradeLog tradeLog = tradeLogRepo.getReferenceById(openDTO.id());
		double capital = cashService.getCapital();

		EvalInDTO evalInDTO = new EvalInDTO(
				tradeLog.getBroker().getId(),
				tradeLog.getTicker().getId(),
				tradeLog.getPriceOpen(),
				tradeLog.getAtr(),
				tradeLog.getItemNumber(),
				openDTO.stopLoss(),
				openDTO.takeProfit(),
				LocalDate.now(),
				tradeLog.isShort()
		);

		double risk = cashService.getRisk(evalInDTO, tradeLog.getBroker().getName(), CurrencyMapper.toDTO(tradeLog.getCurrency()), capital);

		tradeLog.setStopLoss(openDTO.stopLoss());
		tradeLog.setTakeProfit(openDTO.takeProfit());
		tradeLog.setNote(openDTO.note());
		tradeLog.setRisk(risk);

		tradeLogRepo.save(tradeLog);
	}
}
