package com.corn.trade.web.service;

import com.corn.trade.web.dto.TickerDTO;
import com.corn.trade.web.mapper.TickerMapper;
import com.corn.trade.web.repository.TickerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TickerService {

	private final TickerRepository repository;

	public TickerService(TickerRepository repository) {
		this.repository = repository;
	}

	public List<TickerDTO> getAll() {
		return repository.findAll().stream().map(TickerMapper::toDTO).toList();
	}

}
