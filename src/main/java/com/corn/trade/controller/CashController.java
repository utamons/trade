package com.corn.trade.controller;

import com.corn.trade.dto.*;
import com.corn.trade.service.CashService;
import com.corn.trade.service.CurrencyRateService;
import com.corn.trade.service.TradeLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

	@PostMapping(value="/refill", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE )
	public CashAccountDTO refill(@RequestBody TransferDTO transferDTO) {
		return service.refill(transferDTO);
	}

	@PostMapping(value="/exchange", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public CashAccountDTO exchange(@RequestBody ExchangeDTO exchangeDTO) {
		return service.exchange(exchangeDTO);
	}

	@GetMapping("/state")
	public MoneyStateDTO state() throws JsonProcessingException {
		BigDecimal        capital     = service.getCapital();
		List<TradeLogDTO> closed      = tradeLogService.getAllClosed();
		BigDecimal        sumOutcomes = BigDecimal.ZERO;
		final LocalDate   today       = LocalDate.now();

		for (TradeLogDTO dto : closed) {
			sumOutcomes = sumOutcomes.add(
					currencyRateService.convertToUSD(dto.getCurrency().getId(),
					                                 dto.getOutcome(),
					                                 today)
			);
		}

		BigDecimal profit = sumOutcomes.divide(capital, 12, RoundingMode.HALF_EVEN)
		                               .multiply(BigDecimal.valueOf(100.0))
		                               .setScale(2, RoundingMode.HALF_EVEN);

		return new MoneyStateDTO(capital, profit);
	}
}
