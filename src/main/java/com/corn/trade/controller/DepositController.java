package com.corn.trade.controller;

import com.corn.trade.dto.DepositOutDTO;
import com.corn.trade.dto.TickerDTO;
import com.corn.trade.service.DepositService;
import com.corn.trade.service.TickerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/deposit")
public class DepositController {

	private final DepositService service;

	public DepositController(DepositService service) {
		this.service = service;
	}

	@GetMapping("/last")
	public DepositOutDTO getLast(@RequestParam("brokerId") Long brokerId,
	                             @RequestParam("currencyId") Long currencyId) {
		return service.lastDeposit(currencyId, brokerId);
	}

}
