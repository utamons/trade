package com.corn.trade.service;

import com.corn.trade.dto.MarketDTO;
import com.corn.trade.mapper.MarketMapper;
import com.corn.trade.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketService {

	private final MarketRepository repository;

	public MarketService(MarketRepository repository) {
		this.repository = repository;
	}

	public List<MarketDTO> getAll() {
		return repository.findAll().stream().map(MarketMapper::toDTO).collect(Collectors.toList());
	}

}
