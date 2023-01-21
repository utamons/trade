package com.corn.trade.controller;

import com.corn.trade.dto.*;
import com.corn.trade.service.CashService;
import com.corn.trade.service.TradeLogService;
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

	@PostMapping("/open")
	public void open(@RequestBody TradeLogOpenDTO openDTO) {
		service.open(openDTO);
	}

	@PostMapping("/close")
	public void close(@RequestBody TradeLogCloseDTO closeDTO) {
		service.close(closeDTO);
	}

	@PostMapping("/page")
	public TradeLogPageDTO page(@RequestBody TradeLogPageReqDTO pageReqDTO) {
		return service.page(pageReqDTO);
	}
}
