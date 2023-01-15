package com.corn.trade.service;

import com.corn.trade.dto.TickerDTO;
import com.corn.trade.entity.Ticker;
import com.corn.trade.repository.TickerRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class TickerService {

	@Autowired
	private TickerRepository tickerRepository;

	public Long save(TickerDTO dto) {
		Ticker bean = new Ticker();
		BeanUtils.copyProperties(dto, bean);
		bean = tickerRepository.save(bean);
		return bean.getId();
	}

	public void delete(Long id) {
		tickerRepository.deleteById(id);
	}

	public void update(Long id, TickerDTO dto) {
		Ticker bean = requireOne(id);
		BeanUtils.copyProperties(dto, bean);
		tickerRepository.save(bean);
	}

	public TickerDTO getById(Long id) {
		Ticker original = requireOne(id);
		return toDTO(original);
	}

	public Page<TickerDTO> query(TickerDTO dto) {
		throw new UnsupportedOperationException();
	}

	private TickerDTO toDTO(Ticker original) {
		TickerDTO bean = new TickerDTO();
		BeanUtils.copyProperties(original, bean);
		return bean;
	}

	private Ticker requireOne(Long id) {
		return tickerRepository.findById(id)
		                       .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
	}
}
