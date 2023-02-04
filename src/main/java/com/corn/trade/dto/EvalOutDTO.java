package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

public class EvalOutDTO {
	private final Double fees;
	private final Double risk;

	public EvalOutDTO(Double fees, Double risk) {
		this.fees = fees;
		this.risk = risk;
	}

	public BigDecimal getFees() {
		return toOutBigDecimal(fees);
	}

	public BigDecimal getRisk() {
		return toOutBigDecimal(risk);
	}
}
