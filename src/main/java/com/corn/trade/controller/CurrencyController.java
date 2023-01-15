package com.corn.trade.controller;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/currency")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	@PostMapping
	public String save(@Valid @RequestBody CurrencyDTO dto) {
		return currencyService.save(dto).toString();
	}

	@DeleteMapping("/{id}")
	public void delete(@Valid @NotNull @PathVariable("id") Long id) {
		currencyService.delete(id);
	}

	@PutMapping("/{id}")
	public void update(@Valid @NotNull @PathVariable("id") Long id,
	                   @Valid @RequestBody CurrencyDTO dto) {
		currencyService.update(id, dto);
	}

	@GetMapping("/{id}")
	public CurrencyDTO getById(@Valid @NotNull @PathVariable("id") Long id) {
		return currencyService.getById(id);
	}

	@GetMapping
	public Page<CurrencyDTO> query(@Valid CurrencyDTO dto) {
		return currencyService.query(dto);
	}
}
