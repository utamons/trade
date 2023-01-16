package com.corn.trade.controller;

import com.corn.trade.dto.TickerDTO;
import com.corn.trade.service.TickerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticker")
public class TickerController {

	private final TickerService service;

	public TickerController(TickerService service) {
		this.service = service;
	}

	@GetMapping("/all")
	public List<TickerDTO> getAll() {
		return service.getAll();
	}

}
