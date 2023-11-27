package com.corn.trade.web.mapper;

import com.corn.trade.web.dto.CashAccountDTO;
import com.corn.trade.web.dto.CashAccountOutDTO;
import com.corn.trade.web.entity.CashAccount;
import com.corn.trade.web.util.Generated;
import com.corn.trade.web.util.Util;

public class CashAccountMapper {

	@Generated
	private CashAccountMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static CashAccountDTO toDTO(CashAccount entity) {
		return new CashAccountDTO(
				entity.getId(),
				entity.getName(),
				CurrencyMapper.toDTO(entity.getCurrency()),
				BrokerMapper.toDTO(entity.getBroker()),
				entity.getType().getName()
				);
	}

	public static CashAccountOutDTO toOutDTO(CashAccountDTO dto, double amount) {
		return new CashAccountOutDTO(
				dto.id(),
				dto.name(),
				dto.currency(),
				dto.broker(),
				dto.type(),
				Util.round(amount)
				);
	}
}
