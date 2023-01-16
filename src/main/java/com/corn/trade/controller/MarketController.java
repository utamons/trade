package com.corn.trade.controller;

import com.corn.trade.dto.MarketDTO;
import com.corn.trade.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

	private final MarketService service;

	public MarketController(MarketService service) {
		this.service = service;
	}

	@GetMapping("/all")
	public List<MarketDTO> getAll() {
		return service.getAll();
	}

}
