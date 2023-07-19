package com.corn.trade.service;

import com.corn.trade.api.CurrencyAPI;
import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.CurrencyRate;
import com.corn.trade.mapper.CurrencyMapper;
import com.corn.trade.mapper.CurrencyRateMapper;
import com.corn.trade.repository.CurrencyRateRepository;
import com.corn.trade.repository.CurrencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyRateService {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

	private static final Map<LocalDate, Set<CurrencyRateDTO>> cache = new HashMap<>();

	private static List<Currency> currencies;

	public static boolean IS_EXTERNAL_STARTED = false;

	private final CurrencyRateRepository repository;

	private final CurrencyAPI currencyAPI = new CurrencyAPI();

	public CurrencyRateService(CurrencyRateRepository repository, CurrencyRepository currencyRepository,
	                           EntityManager entityManager) {
		this.repository = repository;
		currencies = currencyRepository.findAll();
		currencies.forEach(entityManager::detach);
	}

	public Currency getCurrencyByName(String name) {
		return currencies.stream().filter(currency -> currency.getName().equals(name)).findFirst().orElse(null);
	}

	public Currency getCurrencyById(Long id) {
		return currencies.stream().filter(currency -> currency.getId() == id).findFirst().orElse(null);
	}

	public CurrencyRateDTO findByDate(Long currencyId, LocalDate dateTime) throws JsonProcessingException {
		Currency             currency    = getCurrencyById(currencyId);
		CurrencyDTO          currencyDTO = CurrencyMapper.toDTO(currency);
		Set<CurrencyRateDTO> rates       = cache.computeIfAbsent(dateTime, k -> new HashSet<>());

		CurrencyRateDTO currencyRateDTO = rates.stream()
		                                       .filter(rate -> Objects.equals(rate.currency()
		                                                                          .getId(),
		                                                                      currencyDTO.getId()))
		                                       .findFirst()
		                                       .orElse(null);

		if (currencyRateDTO != null) {
			return currencyRateDTO;
		}

		CurrencyRate    currencyRate = repository.findRateByCurrencyAndDate(currency, dateTime);
		CurrencyRateDTO rateDTO      = CurrencyRateMapper.toDTO(currencyRate);
		if (rateDTO != null) {
			rates.add(rateDTO);
			return rateDTO;
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
				rateDTO      = CurrencyRateMapper.toDTO(currencyRate);
				logger.info("The external API call is finished...");

				rates.add(rateDTO);
				return rateDTO;
			}
			return getExternalRate(currencyId, dateTime);
		}

	}

	public Double convertToUSD(long currencyId, Double amount, LocalDate dateTime) throws JsonProcessingException {
		Currency usd = getCurrencyByName("USD");
		if (usd.getId() == currencyId)
			return amount;
		CurrencyRateDTO currencyRateDTO = findByDate(currencyId, dateTime);
		return amount / currencyRateDTO.rate();
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
				Currency currency = getCurrencyByName(dto.currency().getName());

				CurrencyRate rate = CurrencyRateMapper.toEntity(dto, currency);
				repository.save(rate);
				repository.flush();
			}
		} catch (Exception e) {
			logger.debug("SQL exception: {}", e.getMessage());
		}

		Currency     currency     = getCurrencyById(currencyId);
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, date);

		IS_EXTERNAL_STARTED = false;
		return CurrencyRateMapper.toDTO(currencyRate);
	}
}
