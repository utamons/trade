package com.corn.trade.controller;

import com.corn.trade.dto.*;
import com.corn.trade.service.CashService;
import com.corn.trade.service.CurrencyRateService;
import com.corn.trade.service.TradeLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/cash")
public class CashController {

	private final CashService     service;

	public CashController(CashService service, TradeLogService tradeLogService, CurrencyRateService currencyRateService) {
		this.service = service;
	}

	@PostMapping(value = "/refill", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
			MediaType.APPLICATION_JSON_VALUE)
	public CashAccountDTO refill(@RequestBody TransferDTO transferDTO) {
		return service.refill(transferDTO);
	}

	@PostMapping(value = "/correction", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
			MediaType.APPLICATION_JSON_VALUE)
	public void correction(@RequestBody TransferDTO transferDTO) {
		service.correction(transferDTO);
	}

	@PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void exchange(@RequestBody ExchangeDTO exchangeDTO) {
		service.exchange(exchangeDTO);
	}

	@PostMapping(value = "/eval", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public EvalOutDTO eval(@RequestBody EvalInDTO evalDTO) throws JsonProcessingException {
		return service.eval(evalDTO);
	}

	@PostMapping(value = "/evaltofit", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
			MediaType.APPLICATION_JSON_VALUE)
	public EvalOutFitDTO evalToFit(@RequestBody EvalInFitDTO evalDTO) throws JsonProcessingException {
		return service.evalToFit(evalDTO);
	}
}
