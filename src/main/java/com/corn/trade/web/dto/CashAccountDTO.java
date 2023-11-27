package com.corn.trade.web.dto;

public record CashAccountDTO(Long id, String name, CurrencyDTO currency, BrokerDTO broker, String type) {
}
