package com.corn.trade.dto;

public record EvalOutDTO(Double outcomeExp, Double gainPc, Double fees, Double riskPc, Double riskRewardPc, Double breakEven,
                         Double volume) {
}
