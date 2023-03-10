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
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyRateService {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

	public static boolean IS_EXTERNAL_STARTED = false;

	private final CurrencyRateRepository repository;
	private final CurrencyRepository     currencyRepository;

	private final CurrencyAPI currencyAPI = new CurrencyAPI();

	public CurrencyRateService(CurrencyRateRepository repository, CurrencyRepository currencyRepository) {
		this.repository = repository;
		this.currencyRepository = currencyRepository;
	}

	public CurrencyRateDTO findByDate(Long currencyId, LocalDate dateTime) throws JsonProcessingException {
		Currency     currency     = currencyRepository.getReferenceById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);
		if (currencyRate != null) {
			return CurrencyRateMapper.toDTO(currencyRate);
		} else {
			if (IS_EXTERNAL_STARTED) {
				logger.info("Waiting for the external API call to finish...");
				while (IS_EXTERNAL_STARTED) {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);
				logger.info("The external API call is finished...");
				return CurrencyRateMapper.toDTO(currencyRate);
			}
			return getExternalRate(currencyId, dateTime);
		}

	}

	public Double convertToUSD(long currencyId, Double amount, LocalDate dateTime) throws JsonProcessingException {
		Currency usd = currencyRepository.findCurrencyByName("USD");
		if (usd.getId() == currencyId)
			return amount;
		CurrencyRateDTO currencyRateDTO = findByDate(currencyId, dateTime);
		return amount / currencyRateDTO.getRate();
	}

	private CurrencyRateDTO getExternalRate(Long currencyId, LocalDate date) throws JsonProcessingException {
		IS_EXTERNAL_STARTED = true;
		List<CurrencyRateDTO> rates = currencyAPI.getRatesAt(date);
		if (rates.size() == 0) {
			IS_EXTERNAL_STARTED = false;
			return null;
		}

		try {
			for (CurrencyRateDTO dto : rates) {
				Currency currency = currencyRepository.findCurrencyByName(dto.getCurrency().getName());

				CurrencyRate rate = CurrencyRateMapper.toEntity(dto, currency);
				repository.save(rate);
				repository.flush();
			}
		} catch (Exception e) {
			logger.debug("SQL exception: {}", e.getMessage());
		}

		Currency     currency     = currencyRepository.getReferenceById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, date);

		IS_EXTERNAL_STARTED = false;
		return CurrencyRateMapper.toDTO(currencyRate);
	}
}
