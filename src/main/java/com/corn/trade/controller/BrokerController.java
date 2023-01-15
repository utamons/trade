package com.corn.trade.controller;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/broker")
public class BrokerController {

	@Autowired
	private BrokerService brokerService;

	@PostMapping
	public String save(@Valid @RequestBody BrokerDTO dto) {
		return brokerService.save(dto).toString();
	}

	@DeleteMapping("/{id}")
	public void delete(@Valid @NotNull @PathVariable("id") Long id) {
		brokerService.delete(id);
	}

	@PutMapping("/{id}")
	public void update(@Valid @NotNull @PathVariable("id") Long id,
	                   @Valid @RequestBody BrokerDTO dto) {
		brokerService.update(id, dto);
	}

	@GetMapping("/{id}")
	public BrokerDTO getById(@Valid @NotNull @PathVariable("id") Long id) {
		return brokerService.getById(id);
	}

	@GetMapping
	public Page<BrokerDTO> query(@Valid BrokerDTO dto) {
		return brokerService.query(dto);
	}
}
