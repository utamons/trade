package com.corn.trade.controller;

import com.corn.trade.dto.MarketDTO;
import com.corn.trade.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/market")
public class MarketController {

	@Autowired
	private MarketService marketService;

	@PostMapping
	public String save(@Valid @RequestBody MarketDTO dto) {
		return marketService.save(dto).toString();
	}

	@DeleteMapping("/{id}")
	public void delete(@Valid @NotNull @PathVariable("id") Long id) {
		marketService.delete(id);
	}

	@PutMapping("/{id}")
	public void update(@Valid @NotNull @PathVariable("id") Long id,
	                   @Valid @RequestBody MarketDTO dto) {
		marketService.update(id, dto);
	}

	@GetMapping("/{id}")
	public MarketDTO getById(@Valid @NotNull @PathVariable("id") Long id) {
		return marketService.getById(id);
	}

	@GetMapping
	public Page<MarketDTO> query(@Valid MarketDTO dto) {
		return marketService.query(dto);
	}
}
