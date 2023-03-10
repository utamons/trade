package com.corn.trade.api;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.dto.CurrencyRateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CurrencyAPI {

	public static final Logger logger = LoggerFactory.getLogger(CurrencyAPI.class);
	private final RestTemplate restTemplate = new RestTemplate();

	public List<CurrencyRateDTO> getRatesAt(LocalDate date) throws JsonProcessingException {
		final boolean today = date.equals(LocalDate.now());
		final String currencyKey =  System.getProperty("currency.key");

		if (currencyKey == null) {
			throw new RuntimeException("Currency API key not found");
		}

		String baseURL = "https://api.currencyapi.com/v3/";
		String url     =  today? baseURL + "latest" : baseURL + "historical";

        url += "?apikey="+currencyKey;
		if (!today) {
			url += "&base_currency=USD";
			url += "&date="+date;
		}

		String response = restTemplate.getForObject(url, String.class);

		List<CurrencyRateDTO> result = new ArrayList<>();
		if (response != null) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(response);
			JsonNode data = node.get("data");

			result.add(new CurrencyRateDTO(null,
			                               date,
			                               new CurrencyDTO(null, "KZT"),
			                               data.get("KZT").get("value").doubleValue()));
			result.add(new CurrencyRateDTO(null,
			                               date,
			                               new CurrencyDTO(null, "EUR"),
			                               data.get("EUR").get("value").doubleValue()));
			result.add(new CurrencyRateDTO(null,
			                               date,
			                               new CurrencyDTO(null, "GBP"),
			                               data.get("GBP").get("value").doubleValue()));
			result.add(new CurrencyRateDTO(null,
			                               date,
			                               new CurrencyDTO(null, "HKD"),
			                               data.get("HKD").get("value").doubleValue()));

		}
		return result;
	}
}
