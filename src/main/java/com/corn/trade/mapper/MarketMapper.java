package com.corn.trade.mapper;

import com.corn.trade.dto.MarketDTO;
import com.corn.trade.entity.Market;
import com.corn.trade.util.Generated;

public class MarketMapper {

	@Generated
	private MarketMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static MarketDTO toDTO(Market entity) {
		return new MarketDTO(entity.getId(), entity.getName(), entity.getTimezone());
	}
}
