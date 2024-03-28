package com.corn.trade.broker;

public record OrderBracketIds(int parentId, int stopLossId, int takeProfitId) {}
