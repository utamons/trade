package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.CurrencyRateDTO;
import com.corn.trade.web.entity.Currency;
import com.corn.trade.web.entity.CurrencyRate;
import com.corn.trade.web.util.Generated;

public class CurrencyRateMapper {

	@Generated
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
