package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

public class EvalOutDTO {
	private final Double fees;
	private final Double risk;

	private final Double breakEven;

	public EvalOutDTO(Double fees, Double risk, Double breakEven) {
		this.fees = fees;
		this.risk = risk;
		this.breakEven = breakEven;
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
}
