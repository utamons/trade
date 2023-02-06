package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

public class EvalOutDTO {
	private final Double fees;
	private final Double risk;
	private final Double breakEven;

	private final Double takeProfit;

	private final Double outcomeExp;

	public EvalOutDTO(Double fees, Double risk, Double breakEven, Double takeProfit, Double outcomeExp) {
		this.fees = fees;
		this.risk = risk;
		this.breakEven = breakEven;
		this.takeProfit = takeProfit;
		this.outcomeExp = outcomeExp;
	}

	public BigDecimal getFees() {
		return toOutBigDecimal(fees);
	}

	public BigDecimal getRisk() {
		return toOutBigDecimal(risk);
	}

	public BigDecimal getBreakEven() {
		return toOutBigDecimal(breakEven);
	}

	public BigDecimal getTakeProfit() {
		return toOutBigDecimal(takeProfit);
	}

	public BigDecimal getOutcomeExp() {
		return toOutBigDecimal(outcomeExp);
	}
}
