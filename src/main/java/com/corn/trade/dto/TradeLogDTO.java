package com.corn.trade.dto;

import java.time.LocalDateTime;

import static com.corn.trade.util.Util.round;

public record TradeLogDTO(Long id, String position, LocalDateTime dateOpen, LocalDateTime dateClose, BrokerDTO broker,
                          MarketDTO market, TickerDTO ticker, CurrencyDTO currency, Long itemNumber,
                          Double estimatedPriceOpen, Double estimatedStopLoss, Double estimatedTakeProfit,
                          Double estimatedFees, Double estimatedBreakEven, Double levelPrice, Double atr,
                          Double averagePriceClose, String note, Double brokerInterest, Double totalBought, Double totalSold,
                          Double stopLoss, Double takeProfit, Double openCommission, Double closeCommission) {

}
