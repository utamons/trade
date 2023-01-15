package com.corn.trade.service;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.repository.BrokerRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class BrokerService {

	@Autowired
	private BrokerRepository brokerRepository;

	public Long save(BrokerDTO dto) {
		Broker bean = new Broker();
		BeanUtils.copyProperties(dto, bean);
		bean = brokerRepository.save(bean);
		return bean.getId();
	}

	public void delete(Long id) {
		brokerRepository.deleteById(id);
	}

	public void update(Long id, BrokerDTO dto) {
		Broker bean = requireOne(id);
		BeanUtils.copyProperties(dto, bean);
		brokerRepository.save(bean);
	}

	public BrokerDTO getById(Long id) {
		Broker original = requireOne(id);
		return toDTO(original);
	}

	public Page<BrokerDTO> query(BrokerDTO dto) {
		throw new UnsupportedOperationException();
	}

	private BrokerDTO toDTO(Broker original) {
		BrokerDTO bean = new BrokerDTO();
		BeanUtils.copyProperties(original, bean);
		return bean;
	}

	private Broker requireOne(Long id) {
		return brokerRepository.findById(id)
		                       .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
	}
}
