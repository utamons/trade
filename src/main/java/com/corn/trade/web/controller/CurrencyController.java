package com.corn.trade.web.controller;

import com.corn.trade.web.dto.CurrencyDTO;
import com.corn.trade.web.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

	private final CurrencyService service;

	public CurrencyController(CurrencyService service) {
		this.service = service;
	}

	@GetMapping("/all")
	public List<CurrencyDTO> getAll() {
		return service.getAll();
	}

}
