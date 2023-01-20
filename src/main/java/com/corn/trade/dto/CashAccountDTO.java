package com.corn.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class CashAccountDTO {

	private final Long id;

	private final String name;

	private final CurrencyDTO currency;

	private final BrokerDTO broker;

	private final String type;

	private final BigDecimal amount;

	private final LocalDateTime updatedAt;

	public CashAccountDTO(Long id,
	                      String name,
	                      CurrencyDTO currency,
	                      BrokerDTO broker,
	                      String type,
	                      BigDecimal amount,
	                      LocalDateTime updatedAt) {
		this.id = id;
		this.name = name;
		this.currency = currency;
		this.broker = broker;
		this.type = type;
		this.amount = amount;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}

	public BrokerDTO getBroker() {
		return broker;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
