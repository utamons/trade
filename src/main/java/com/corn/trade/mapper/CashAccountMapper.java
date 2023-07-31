package com.corn.trade.mapper;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.entity.CashAccount;

public class CashAccountMapper {
	public static CashAccountDTO toDTO(CashAccount entity) {
		return new CashAccountDTO(
				entity.getId(),
				entity.getName(),
				CurrencyMapper.toDTO(entity.getCurrency()),
				BrokerMapper.toDTO(entity.getBroker()),
				entity.getType().getName()
				);
	}
}
