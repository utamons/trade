package com.corn.trade.dto;

import java.math.BigDecimal;

public record CashAccountOutDTO(Long id, String name, CurrencyDTO currency, BrokerDTO broker, String type, BigDecimal amount) {}
