package com.corn.trade.mapper;

import com.corn.trade.dto.TickerDTO;
import com.corn.trade.entity.Ticker;

public class TickerMapper {
	public static TickerDTO toDTO(Ticker entity) {
		return new TickerDTO(entity.getId(), entity.getName(), entity.getShortName(), CurrencyMapper.toDTO(entity.getCurrency()));
	}
}
