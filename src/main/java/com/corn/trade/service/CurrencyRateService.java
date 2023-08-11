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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.corn.trade.util.Util.round;

@Service
public class CurrencyRateService {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

	private static final Map<LocalDate, Set<CurrencyRateDTO>> cache = new HashMap<>();

	private static List<Currency> currencies;

	private static boolean isExternalStarted = false;

	private final CurrencyRateRepository repository;

	private final CurrencyAPI currencyAPI = new CurrencyAPI();

	public CurrencyRateService(CurrencyRateRepository repository, CurrencyRepository currencyRepository) {
		logger.info("CurrencyRateService is created...");
		this.repository = repository;
		if (currencies == null) {
			currencies = currencyRepository.findAll();
			currencies.forEach(currency -> currency.setName(currency.getName().trim())); // fucking H2!
			logger.info("{} currencies were loaded", currencies.size());
		}
	}

	public Currency getCurrencyByName(String name) {
		return currencies.stream().filter(currency -> currency.getName().equals(name)).findFirst().orElse(null);
	}

	public Currency getCurrencyById(Long id) {
		return currencies.stream().filter(currency -> currency.getId() == id).findFirst().orElse(null);
	}

	public CurrencyRateDTO findInCache(CurrencyDTO currencyDTO, LocalDate date) {
		Set<CurrencyRateDTO> rates = cache.computeIfAbsent(date, k -> new HashSet<>());

		return rates.stream()
		            .filter(rate -> Objects.equals(rate.currency().id(), currencyDTO.id()))
		            .findFirst()
		            .orElse(null);
	}

	public CurrencyRateDTO findByDate(Long currencyId, LocalDate date) throws JsonProcessingException {
		logger.debug("start");
		Currency        currency        = getCurrencyById(currencyId);
		CurrencyDTO     currencyDTO     = CurrencyMapper.toDTO(currency);
		CurrencyRateDTO currencyRateDTO = findInCache(currencyDTO, date);

		if (currencyRateDTO != null) {
			return currencyRateDTO;
		}

		CurrencyRate    currencyRate = repository.findRateByCurrencyAndDate(currency, date);
		CurrencyRateDTO rateDTO      = CurrencyRateMapper.toDTO(currencyRate);
		if (rateDTO != null) {
			Set<CurrencyRateDTO> rates = cache.computeIfAbsent(date, k -> new HashSet<>());
			rates.add(rateDTO);
			logger.debug("finish");
			return rateDTO;
		} else {
			if (isExternalStarted) {
				logger.info("Waiting for the external API call to finish...");
				while (isExternalStarted) {
					try {
						TimeUnit.MILLISECONDS.sleep(300);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				rateDTO = findInCache(currencyDTO, date);
				logger.info("The external API call is finished...");
				if (rateDTO == null)
					throw new IllegalStateException("The external API call is finished, but the rate is still null");
				logger.debug("finish");
				return rateDTO;
			}
			logger.debug("finish");
			return getExternalRate(currencyId, date);
		}
	}

	@Transactional
	public Double convertToUSD(long currencyId, Double amount, LocalDate dateTime) throws JsonProcessingException {
		Currency usd = getCurrencyByName("USD");
		if (usd.getId() == currencyId)
			return amount;
		CurrencyRateDTO currencyRateDTO = findByDate(currencyId, dateTime);
		return round(amount / currencyRateDTO.rate(), 2);
	}

	public Double convert(Currency fromCurrency,
	                      Currency toCurrency,
	                      Double amount,
	                      LocalDate dateTime) throws JsonProcessingException {
		long fromCurrencyId = fromCurrency.getId();
		long toCurrencyId   = toCurrency.getId();
		if (fromCurrencyId == toCurrencyId)
			return amount;
		CurrencyRateDTO fromCurrencyRateDTO = findByDate(fromCurrencyId, dateTime);
		CurrencyRateDTO toCurrencyRateDTO   = findByDate(toCurrencyId, dateTime);
		return amount * toCurrencyRateDTO.rate() / fromCurrencyRateDTO.rate();
	}

	public CurrencyRateDTO getExternalRate(Long currencyId, LocalDate date) throws JsonProcessingException {
		isExternalStarted = true;
		List<CurrencyRateDTO> rates = currencyAPI.getRatesAt(date);
		if (rates.isEmpty()) {
			isExternalStarted = false;
			return null;
		}

		try {
			Set<CurrencyRateDTO> cacheRates = cache.computeIfAbsent(date, k -> new HashSet<>());
			for (CurrencyRateDTO dto : rates) {
				Currency currency = getCurrencyByName(dto.currency().name());

				CurrencyRate rate = CurrencyRateMapper.toEntity(dto, currency);
				rate = repository.save(rate);
				cacheRates.add(CurrencyRateMapper.toDTO(rate));
			}
			repository.flush();
			logger.debug("{} rates were saved", rates.size());
		} catch (Exception e) {
			logger.debug("SQL exception: {}", e.getMessage());
		}

		Currency currency = getCurrencyById(currencyId);
		logger.debug("Getting a rate for currency: {}, {}", currency.getId(), currency.getName());
		CurrencyRate currencyRate = repository.findRateByCurrencyAndDate(currency, date);

		isExternalStarted = false;
		return CurrencyRateMapper.toDTO(currencyRate);
	}
}
