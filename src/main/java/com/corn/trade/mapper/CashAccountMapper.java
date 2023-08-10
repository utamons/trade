package com.corn.trade.mapper;

import com.corn.trade.dto.CashAccountDTO;
import com.corn.trade.dto.CashAccountOutDTO;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.util.Generated;

import static com.corn.trade.util.Util.round;

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
				round(amount)
				);
	}
}
