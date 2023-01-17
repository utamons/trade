package com.corn.trade.mapper;

import com.corn.trade.dto.CurrencyRateDTO;
import com.corn.trade.entity.CurrencyRate;

public class CurrencyRateMapper {
	public static CurrencyRateDTO toDTO(CurrencyRate entity) {
		return new CurrencyRateDTO(
				entity.getId(),
				entity.getDate(),
				CurrencyMapper.toDTO(entity.getCurrency()),
				entity.getRate());
	}
}
