package com.corn.trade.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeLogDTO(Long id, String position, LocalDateTime dateOpen, LocalDateTime dateClose, BrokerDTO broker,
                          MarketDTO market, TickerDTO ticker, CurrencyDTO currency,
                          //-----------------------------
                          BigDecimal estimatedPriceOpen, BigDecimal estimatedFees, BigDecimal estimatedBreakEven,
                          Long estimatedItems, BigDecimal riskToCapitalPc, BigDecimal risk, BigDecimal levelPrice,
                          BigDecimal atr,
                          //------------------------------
                          BigDecimal openStopLoss, BigDecimal openTakeProfit, BigDecimal brokerInterest, BigDecimal totalBought,
                          BigDecimal totalSold, Long itemBought, Long itemSold, BigDecimal finalStopLoss,
                          BigDecimal finalTakeProfit, BigDecimal openCommission, BigDecimal closeCommission,
                          Long partsClosed, String note,
                          //------------------------------
                          BigDecimal outcome, BigDecimal outcomePc) {}
