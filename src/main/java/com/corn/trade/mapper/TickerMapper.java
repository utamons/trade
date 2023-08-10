package com.corn.trade.mapper;

import com.corn.trade.dto.TickerDTO;
import com.corn.trade.entity.Ticker;
import com.corn.trade.util.Generated;

public class TickerMapper {

	@Generated
	private TickerMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static TickerDTO toDTO(Ticker entity) {
		return new TickerDTO(entity.getId(), entity.getLongName(), entity.getName(), CurrencyMapper.toDTO(entity.getCurrency()));
	}
}
