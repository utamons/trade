package com.corn.trade.service;

import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.CurrencyRate;
import com.corn.trade.mapper.CurrencyRateMapper;
import com.corn.trade.repository.CurrencyRateRepository;
import com.corn.trade.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CurrencyRateService {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

	private final CurrencyRateRepository repository;
	private final CurrencyRepository currencyRepository;

	public CurrencyRateService(CurrencyRateRepository repository, CurrencyRepository currencyRepository) {
		this.repository = repository;
		this.currencyRepository = currencyRepository;
	}

	public CurrencyRateDTO findByDate(Long currencyId, LocalDate dateTime) {
		logger.info("Trying to find for a date {}", dateTime);
		Currency currency = currencyRepository.getReferenceById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);
		if (currencyRate != null) {
			return CurrencyRateMapper.toDTO(currencyRate);
		} else
			return null;
	}
}
