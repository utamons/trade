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
	private final TradeLogService tradeLogService;

	private final CurrencyRateService currencyRateService;

	public CashController(CashService service, TradeLogService tradeLogService, CurrencyRateService currencyRateService) {
		this.service = service;
		this.tradeLogService = tradeLogService;
		this.currencyRateService = currencyRateService;
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

	@PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
			MediaType.APPLICATION_JSON_VALUE)
	public CashAccountDTO exchange(@RequestBody ExchangeDTO exchangeDTO) {
		return service.exchange(exchangeDTO);
	}

	@GetMapping("/state")
	public MoneyStateDTO state() throws JsonProcessingException {
		double            capital     = service.getCapital();
		List<TradeLogDTO> closed      = tradeLogService.getAllClosed();
		double            sumOutcomes = 0.0;
		final LocalDate   today       = LocalDate.now();

		for (TradeLogDTO dto : closed) {
			sumOutcomes = sumOutcomes +
			              currencyRateService.convertToUSD(dto.getCurrency().getId(),
			                                               dto.getOutcomeDouble(),
			                                               today);
		}

		double profit = capital == 0 ? 0.0 : sumOutcomes / capital * 100.0;

		double riskBase = service.getRiskBase(capital);

		return new MoneyStateDTO(capital, profit, riskBase);
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
