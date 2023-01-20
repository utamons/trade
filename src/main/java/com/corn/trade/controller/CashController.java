package com.corn.trade.controller;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.ExchangeDTO;
import com.corn.trade.dto.TransferDTO;
import com.corn.trade.service.CashService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/cash")
public class CashController {

	private final CashService service;

	public CashController(CashService service) {
		this.service = service;
	}

	@PostMapping("/refill")
	public CashAccountDTO refill(@RequestBody TransferDTO transferDTO) {
		return service.refill(transferDTO);
	}

	@PostMapping("/exchange")
	public CashAccountDTO refill(@RequestBody ExchangeDTO exchangeDTO) {
		return service.exchange(exchangeDTO);
	}

	@PostMapping("/fee")
	public CashAccountDTO fee(@RequestBody TransferDTO transferDTO) {
		return service.fee(transferDTO);
	}

	@PostMapping("/buy")
	public CashAccountDTO buy(@RequestBody TransferDTO transferDTO) {
		return service.buy(transferDTO);
	}
}
