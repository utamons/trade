package com.corn.trade.controller;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.dto.BrokerStatsDTO;
import com.corn.trade.service.BrokerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("unused")
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

	@GetMapping("/stats")
	public BrokerStatsDTO getStats(@RequestParam("brokerId") Long brokerId) throws JsonProcessingException {
		return service.getStats(brokerId);
	}

}
