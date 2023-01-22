package com.corn.trade.service;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.mapper.BrokerMapper;
import com.corn.trade.repository.BrokerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrokerService {

	private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);
	private final BrokerRepository repository;

	public BrokerService(BrokerRepository repository) {
		this.repository = repository;
	}

	public List<BrokerDTO> getAll() {
		logger.debug("Invoke");
		return repository.findAll().stream().map(BrokerMapper::toDTO).collect(Collectors.toList());
	}

}
