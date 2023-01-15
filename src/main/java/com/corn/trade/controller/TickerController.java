package com.corn.trade.controller;

import com.corn.trade.dto.TickerDTO;
import com.corn.trade.service.TickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/ticker")
public class TickerController {

	@Autowired
	private TickerService tickerService;

	@PostMapping
	public String save(@Valid @RequestBody TickerDTO dto) {
		return tickerService.save(dto).toString();
	}

	@DeleteMapping("/{id}")
	public void delete(@Valid @NotNull @PathVariable("id") Long id) {
		tickerService.delete(id);
	}

	@PutMapping("/{id}")
	public void update(@Valid @NotNull @PathVariable("id") Long id,
	                   @Valid @RequestBody TickerDTO dto) {
		tickerService.update(id, dto);
	}

	@GetMapping("/{id}")
	public TickerDTO getById(@Valid @NotNull @PathVariable("id") Long id) {
		return tickerService.getById(id);
	}

	@GetMapping
	public Page<TickerDTO> query(@Valid TickerDTO dto) {
		return tickerService.query(dto);
	}
}
