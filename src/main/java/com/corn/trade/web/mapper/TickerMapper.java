package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.TickerDTO;
import com.corn.trade.web.entity.Ticker;
import com.corn.trade.web.util.Generated;

public class TickerMapper {

	@Generated
	private TickerMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static TickerDTO toDTO(Ticker entity) {
		return new TickerDTO(entity.getId(), entity.getLongName(), entity.getName(), CurrencyMapper.toDTO(entity.getCurrency()));
	}
}
