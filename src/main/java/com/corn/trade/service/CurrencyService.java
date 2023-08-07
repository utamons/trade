package com.corn.trade.service;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.mapper.CurrencyMapper;
import com.corn.trade.repository.CurrencyRepository;
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
