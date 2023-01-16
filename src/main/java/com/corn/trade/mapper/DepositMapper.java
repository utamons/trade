package com.corn.trade.mapper;

import com.corn.trade.dto.DepositOutDTO;
import com.corn.trade.entity.Deposit;

public class DepositMapper {

	public static DepositOutDTO toOutDTO(Deposit deposit) {
		return new DepositOutDTO(
				CurrencyMapper.toDTO(deposit.getCurrency()),
				BrokerMapper.toDTO(deposit.getBroker()),
				deposit.getSum());
	}

}
