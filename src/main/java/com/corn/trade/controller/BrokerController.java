package com.corn.trade.controller;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.service.BrokerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/broker")
public class BrokerController {

	private final BrokerService service;

	public BrokerController(BrokerService service) {
		this.service = service;
	}

	@GetMapping("/all")
	public List<BrokerDTO> getAll() {
		return service.getAll();
	}

}
