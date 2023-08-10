package com.corn.trade.dto;

public record CashAccountOutDTO(Long id, String name, CurrencyDTO currency, BrokerDTO broker, String type, Double amount) {}
