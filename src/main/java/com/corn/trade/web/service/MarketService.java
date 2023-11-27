package com.corn.trade.web.service;

import com.corn.trade.web.dto.MarketDTO;
import com.corn.trade.web.mapper.MarketMapper;
import com.corn.trade.web.repository.MarketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {

	private final MarketRepository repository;

	public MarketService(MarketRepository repository) {
		this.repository = repository;
	}

	public List<MarketDTO> getAll() {
		return repository.findAll().stream().map(MarketMapper::toDTO).toList();
	}

}
