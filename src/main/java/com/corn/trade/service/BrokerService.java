package com.corn.trade.service;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.mapper.BrokerMapper;
import com.corn.trade.repository.BrokerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrokerService {

	private static final Logger           logger = LoggerFactory.getLogger(BrokerService.class);
	private final        BrokerRepository repository;
	private final        CashService      cashService;

	private final CurrencyRateService currencyRateService;

	private final TradeLogService tradeLogService;

	public BrokerService(BrokerRepository repository,
	                     CashService cashService,
	                     CurrencyRateService currencyRateService,
	                     TradeLogService tradeLogService) {
		this.repository = repository;
		this.cashService = cashService;
		this.currencyRateService = currencyRateService;
		this.tradeLogService = tradeLogService;
	}

	public List<BrokerDTO> getAll() {
		logger.debug("Invoke");
		return repository.findAll().stream().map(BrokerMapper::toDTO).collect(Collectors.toList());
	}

	public BrokerStatsDTO getStats(Long brokerId) throws JsonProcessingException {
		List<CashAccountDTO> accounts         = cashService.getTradeAccounts(brokerId);
		List<CashAccountDTO> borrowedAccounts = cashService.getBorrowedAccounts(brokerId);
		List<TradeLogDTO>    dtos             = tradeLogService.getAllClosedByBroker(brokerId);

		double outcome   = 0.0;
		double avgProfit = 0.0;

		LocalDate currentDate = LocalDate.now();

		for (TradeLogDTO dto : dtos) {
			outcome += currencyRateService.convertToUSD(
					dto.getCurrency().getId(),
					dto.getOutcomeDouble(),
					currentDate);
			avgProfit += dto.getOutcomePercentDouble();
		}

		double borrowed = 0.0;

		for (CashAccountDTO b : borrowedAccounts) {
			borrowed += currencyRateService.convertToUSD(b.getCurrency().getId(),
			                                             b.getAmountDouble(),
			                                             LocalDate.now());
		}

		double avgOutcome = dtos.size() > 0 ? outcome / dtos.size() : 0.0;
		avgProfit = dtos.size() > 0 ? avgProfit / dtos.size() : 0.0;

		long open = tradeLogService.getOpenCountByBroker(brokerId);

		return new BrokerStatsDTO(
				accounts,
				outcome,
				avgOutcome,
				avgProfit,
				open,
				borrowed);
	}
}
