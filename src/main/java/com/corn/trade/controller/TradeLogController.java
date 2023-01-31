package com.corn.trade.controller;

import com.corn.trade.dto.*;
import com.corn.trade.service.CashService;
import com.corn.trade.service.TradeLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/log")
public class TradeLogController {

	private final TradeLogService service;

	public TradeLogController(TradeLogService service) {
		this.service = service;
	}

	@PostMapping(value = "/open", consumes= MediaType.APPLICATION_JSON_VALUE)
	public void open(@RequestBody TradeLogOpenDTO openDTO) {
		service.open(openDTO);
	}

	@PostMapping(value = "/close", consumes=MediaType.APPLICATION_JSON_VALUE)
	public void close(@RequestBody TradeLogCloseDTO closeDTO) throws JsonProcessingException {
		service.close(closeDTO);
	}

	@PostMapping(value = "/page", consumes=MediaType.APPLICATION_JSON_VALUE)
	public Page<TradeLogDTO> page(@RequestBody TradeLogPageReqDTO pageReqDTO) {
		return service.getPage(pageReqDTO);
	}
}
