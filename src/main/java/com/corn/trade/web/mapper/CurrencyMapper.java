package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.CurrencyDTO;
import com.corn.trade.web.entity.Currency;
import com.corn.trade.web.util.Generated;

public class CurrencyMapper {

	@Generated
	private CurrencyMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static CurrencyDTO toDTO(Currency entity) {
		return new CurrencyDTO(entity.getId(), entity.getName().trim());
	}
}
