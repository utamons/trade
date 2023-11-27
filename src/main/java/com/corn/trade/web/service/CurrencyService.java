package com.corn.trade.web.service;

import com.corn.trade.web.dto.CurrencyDTO;
import com.corn.trade.web.mapper.CurrencyMapper;
import com.corn.trade.web.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {

	private final CurrencyRepository repository;

	public CurrencyService(CurrencyRepository repository) {
		this.repository = repository;
	}

	public List<CurrencyDTO> getAll() {
		return repository.findAll().stream().map(CurrencyMapper::toDTO).toList();
	}

}
