package com.corn.trade.controller;

import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.service.CurrencyRateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/currency/rate")
public class CurrencyRateController {

	private final CurrencyRateService service;

	public CurrencyRateController(CurrencyRateService service) {
		this.service = service;
	}

	@GetMapping("/find")
	public CurrencyRateDTO find (
			@RequestParam("currencyId") Long currencyId,
			@RequestParam("date")  @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date) {
		return service.findByDate(currencyId, date);
	}

}
