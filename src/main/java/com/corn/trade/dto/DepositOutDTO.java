package com.corn.trade.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DepositOutDTO implements Serializable {

	private static final long serialVersionUID = 4975578334491673886L;
	private final CurrencyDTO currency;
	private final BrokerDTO broker;
	private final BigDecimal sum;

	public DepositOutDTO(CurrencyDTO currency, BrokerDTO broker,BigDecimal sum) {
		this.currency = currency;
		this.broker = broker;
		this.sum = sum;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}

	public BrokerDTO getBroker() {
		return broker;
	}

	public BigDecimal getSum() {
		return sum;
	}
}
