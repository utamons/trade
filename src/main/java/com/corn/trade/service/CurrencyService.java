package com.corn.trade.service;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.entity.Currency;
import com.corn.trade.repository.CurrencyRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CurrencyService {

	@Autowired
	private CurrencyRepository currencyRepository;

	public Long save(CurrencyDTO dto) {
		Currency bean = new Currency();
		BeanUtils.copyProperties(dto, bean);
		bean = currencyRepository.save(bean);
		return bean.getId();
	}

	public void delete(Long id) {
		currencyRepository.deleteById(id);
	}

	public void update(Long id, CurrencyDTO dto) {
		Currency bean = requireOne(id);
		BeanUtils.copyProperties(dto, bean);
		currencyRepository.save(bean);
	}

	public CurrencyDTO getById(Long id) {
		Currency original = requireOne(id);
		return toDTO(original);
	}

	public Page<CurrencyDTO> query(CurrencyDTO dto) {
		throw new UnsupportedOperationException();
	}

	private CurrencyDTO toDTO(Currency original) {
		CurrencyDTO bean = new CurrencyDTO();
		BeanUtils.copyProperties(original, bean);
		return bean;
	}

	private Currency requireOne(Long id) {
		return currencyRepository.findById(id)
		                         .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
	}
}
