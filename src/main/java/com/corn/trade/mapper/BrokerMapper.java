package com.corn.trade.mapper;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.entity.Broker;

public class BrokerMapper {

	private BrokerMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static BrokerDTO toDTO(Broker entity) {
		return new BrokerDTO(entity.getId(), entity.getName(), CurrencyMapper.toDTO(entity.getFeeCurrency()));
	}
}
