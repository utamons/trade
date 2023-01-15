package com.corn.trade.service;

import com.corn.trade.dto.MarketDTO;
import com.corn.trade.entity.Market;
import com.corn.trade.repository.MarketRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class MarketService {

	@Autowired
	private MarketRepository marketRepository;

	public Long save(MarketDTO dto) {
		Market bean = new Market();
		BeanUtils.copyProperties(dto, bean);
		bean = marketRepository.save(bean);
		return bean.getId();
	}

	public void delete(Long id) {
		marketRepository.deleteById(id);
	}

	public void update(Long id, MarketDTO dto) {
		Market bean = requireOne(id);
		BeanUtils.copyProperties(dto, bean);
		marketRepository.save(bean);
	}

	public MarketDTO getById(Long id) {
		Market original = requireOne(id);
		return toDTO(original);
	}

	public Page<MarketDTO> query(MarketDTO dto) {
		throw new UnsupportedOperationException();
	}

	private MarketDTO toDTO(Market original) {
		MarketDTO bean = new MarketDTO();
		BeanUtils.copyProperties(original, bean);
		return bean;
	}

	private Market requireOne(Long id) {
		return marketRepository.findById(id)
		                       .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
	}
}
