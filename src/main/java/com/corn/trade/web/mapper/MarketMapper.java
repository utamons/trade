package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.MarketDTO;
import com.corn.trade.web.entity.Market;
import com.corn.trade.web.util.Generated;

public class MarketMapper {

	@Generated
	private MarketMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static MarketDTO toDTO(Market entity) {
		return new MarketDTO(entity.getId(), entity.getName(), entity.getTimezone());
	}
}
