package com.corn.trade.service;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.mapper.BrokerMapper;
import com.corn.trade.repository.BrokerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
