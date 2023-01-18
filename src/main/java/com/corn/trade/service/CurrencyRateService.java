package com.corn.trade.service;

import com.corn.trade.api.CurrencyAPI;
import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.CurrencyRate;
import com.corn.trade.mapper.CurrencyRateMapper;
import com.corn.trade.repository.CurrencyRateRepository;
import com.corn.trade.repository.CurrencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CurrencyRateService {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

	private final CurrencyRateRepository repository;
	private final CurrencyRepository currencyRepository;

	private final CurrencyAPI currencyAPI = new CurrencyAPI();

	public CurrencyRateService(CurrencyRateRepository repository, CurrencyRepository currencyRepository) {
		this.repository = repository;
		this.currencyRepository = currencyRepository;
	}

	public CurrencyRateDTO findByDate(Long currencyId, LocalDate dateTime) throws JsonProcessingException {
		logger.info("Trying to find for a date {}", dateTime);
		Currency currency = currencyRepository.getReferenceById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);
		if (currencyRate != null) {
			return CurrencyRateMapper.toDTO(currencyRate);
		} else
			return getExternalRate(currencyId, dateTime);
	}

	private CurrencyRateDTO getExternalRate(Long currencyId, LocalDate dateTime) throws JsonProcessingException {
		List<CurrencyRateDTO> rates = currencyAPI.getRatesAt(dateTime);
		if (rates.size() == 0)
			return null;

		for(CurrencyRateDTO dto: rates) {
			Currency currency = currencyRepository.findCurrencyByName(dto.getCurrency().getName());
			CurrencyRate rate = CurrencyRateMapper.toEntity(dto, currency);
			repository.save(rate);
		}

		Currency currency = currencyRepository.getReferenceById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);

		return CurrencyRateMapper.toDTO(currencyRate);
	}
}
