package com.corn.trade.mapper;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.entity.Currency;

public class CurrencyMapper {

	private CurrencyMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static CurrencyDTO toDTO(Currency entity) {
		return new CurrencyDTO(entity.getId(), entity.getName().trim());
	}
}
