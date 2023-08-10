package com.corn.trade.controller;

import com.corn.trade.dto.MoneyStateDTO;
import com.corn.trade.dto.StatsData;
import com.corn.trade.dto.TradeLogDTO;
import com.corn.trade.service.StatsService;
import com.corn.trade.service.TimePeriod;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

	private final StatsService service;

	public StatsController(StatsService service) {
		this.service = service;
	}

	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	public StatsData getStats(@RequestParam TimePeriod timePeriod) {
		return service.getStats(timePeriod);
	}

	@GetMapping("/state")
	public MoneyStateDTO state() throws JsonProcessingException {
		return service.getMoneyState();
	}
}
