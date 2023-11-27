package com.corn.trade.web.dto;

@SuppressWarnings("unused")
public record ExchangeDTO(Long brokerId, Long currencyFromId, Long currencyToId, Double amountFrom, Double amountTo) {


}
