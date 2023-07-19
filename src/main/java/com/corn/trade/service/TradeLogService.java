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
		Broker   broker        = brokerRepo.getReferenceById(openDTO.getBrokerId());
		Market   market        = marketRepo.getReferenceById(openDTO.getMarketId());
		Ticker   ticker        = tickerRepo.getReferenceById(openDTO.getTickerId());
		Double   depositAmount = cashService.lastDepositAmount(broker, ticker.getCurrency());
		TradeLog tradeLog      = TradeLogMapper.toEntity(openDTO, broker, market, ticker, depositAmount);

		tradeLog = tradeLogRepo.save(tradeLog);
		tradeLogRepo.flush();
		if (openDTO.getPosition().equals("long"))
			cashService.buy(tradeLog.getVolume(), broker, ticker.getCurrency(), tradeLog);
		else
			cashService.sellShort(tradeLog.getVolume(), broker, ticker.getCurrency(), tradeLog);
		if (tradeLog.getFees() != 0.0)
			cashService.fee(tradeLog.getFees(), broker, tradeLog, tradeLog.getDateOpen());
	}

	public void close(TradeLogCloseDTO closeDTO) throws JsonProcessingException {
		TradeLog open = tradeLogRepo.getReferenceById(closeDTO.getId());

		if (closeDTO.getQuantity() != null && closeDTO.getQuantity() < open.getItemNumber())
			open = copyPartial(closeDTO, open);

		final Broker   broker   = open.getBroker();
		final Currency currency = open.getCurrency();
		final CurrencyDTO currencyDTO = CurrencyMapper.toDTO(currency);

		final double        priceOpen      = open.getPriceOpen();
		double              priceClose     = closeDTO.getPriceClose();
		final int           shortC         = open.getPosition().equals("long") ? 1 : -1;
		final long          items          = open.getItemNumber();
		final double        sum            = priceClose * items;
		final double        volume         = open.getVolume();
		final LocalDate     dateOpen       = open.getDateOpen().toLocalDate();
		final LocalDateTime dateTimeClose  = closeDTO.getDateClose();
		final LocalDate     dateClose      = dateTimeClose.toLocalDate();
		final String        note           = closeDTO.getNote();
		final double        brokerInterest = closeDTO.getBrokerInterest() == null ? 0.0 : closeDTO.getBrokerInterest();

		// in the currency of the position:
		final double closeFees = cashService.getFees(broker.getName(), currencyDTO, items, sum).getAmount();
		final double openFees  = cashService.getFees(broker.getName(), currencyDTO, items, volume).getAmount();

		final double outcome        = ((shortC * priceClose - shortC * priceOpen) * items) - (closeFees + openFees + brokerInterest);
		final double outcomePercent = outcome / volume * 100.0;

		final double closeFeesUSD = currencyRateService.convertToUSD(currency.getId(), closeFees, dateClose);
		final double openFeesUSD  = currencyRateService.convertToUSD(currency.getId(), openFees, dateOpen);

		if (closeFeesUSD != 0)
			cashService.fee(closeFeesUSD, broker, open, dateTimeClose);
		if (brokerInterest != 0)
			cashService.fee(brokerInterest, broker, open, dateTimeClose);

		double percentToCapital = cashService.percentToCapital(outcome, volume, currency);

		open.setFees(openFeesUSD + closeFeesUSD);
		open.setPriceClose(priceClose);
		open.setDateClose(dateTimeClose);
		open.setOutcome(outcome);
		open.setOutcomePercent(outcomePercent);
		open.setProfit(percentToCapital);
		if (open.isShort()) {
			open.setBrokerInterest(brokerInterest);
		}

		if (note != null)
			open.setNote(note);


		double closeAmount = priceClose * items;

		open = tradeLogRepo.save(open);

		if (open.getPosition().equals("long"))
			cashService.sell(volume, closeAmount, broker, currency, open);
		else
			cashService.buyShort(volume, closeAmount, broker, currency, open);
	}

	private TradeLog copyPartial(TradeLogCloseDTO closeDTO, TradeLog open) {
		Double   depositAmount = cashService.lastDepositAmount(open.getBroker(), open.getCurrency());
		Double volume = open.getPriceOpen()*closeDTO.getQuantity();
		Double volumeToDeposit = volume/depositAmount*100.0;

		TradeLog partial = new TradeLog();
		partial.setPosition(open.getPosition());
		partial.setDateOpen(open.getDateOpen());
		partial.setBroker(open.getBroker());
		partial.setMarket(open.getMarket());
		partial.setTicker(open.getTicker());
		partial.setCurrency(open.getCurrency());
		partial.setItemNumber(closeDTO.getQuantity().longValue());
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

		partial = tradeLogRepo.save(partial);

		open.setItemNumber(open.getItemNumber() - closeDTO.getQuantity());
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
		TradeLog tradeLog = tradeLogRepo.getReferenceById(openDTO.getId());
		double capital = cashService.getCapital();

		EvalInDTO evalInDTO = new EvalInDTO(
				tradeLog.getBroker().getId(),
				tradeLog.getTicker().getId(),
				tradeLog.getPriceOpen(),
				tradeLog.getAtr(),
				tradeLog.getItemNumber(),
				openDTO.getStopLoss(),
				openDTO.getTakeProfit(),
				LocalDate.now(),
				tradeLog.isShort()
		);

		double risk = cashService.getRisk(evalInDTO, tradeLog.getBroker().getName(), CurrencyMapper.toDTO(tradeLog.getCurrency()), capital);

		tradeLog.setStopLoss(openDTO.getStopLoss());
		tradeLog.setTakeProfit(openDTO.getTakeProfit());
		tradeLog.setNote(openDTO.getNote());
		tradeLog.setRisk(risk);

		tradeLogRepo.save(tradeLog);
	}
}
