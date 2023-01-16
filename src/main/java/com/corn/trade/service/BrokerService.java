package com.corn.trade.service;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.mapper.BrokerMapper;
import com.corn.trade.repository.BrokerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrokerService {

	private final BrokerRepository repository;

	public BrokerService(BrokerRepository repository) {
		this.repository = repository;
	}

	public List<BrokerDTO> getAll() {
		return repository.findAll().stream().map(BrokerMapper::toDTO).collect(Collectors.toList());
	}

}
