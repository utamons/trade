package com.corn.trade.mapper;

import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.CurrencyRate;

public class CurrencyRateMapper {

	private CurrencyRateMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static CurrencyRateDTO toDTO(CurrencyRate entity) {
		if (entity == null)
			return null;
		return new CurrencyRateDTO(
				entity.getId(),
				entity.getDate(),
				CurrencyMapper.toDTO(entity.getCurrency()),
				entity.getRate());
	}

	public static CurrencyRate toEntity(CurrencyRateDTO dto, Currency currency) {
		return new CurrencyRate(dto.date(), currency, dto.rate());
	}
}
