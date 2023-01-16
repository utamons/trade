package com.corn.trade.mapper;

import com.corn.trade.dto.CurrencyDTO;
import com.corn.trade.entity.Currency;

public class CurrencyMapper {
	public static CurrencyDTO toDTO(Currency entity) {
		return new CurrencyDTO(entity.getId(), entity.getName());
	}
}
