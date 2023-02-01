package com.corn.trade.dto;

import java.math.BigDecimal;

public class EvalOutDTO {
	private final BigDecimal fees;
	private final BigDecimal risk;

	public EvalOutDTO(BigDecimal fees, BigDecimal risk) {
		this.fees = fees;
		this.risk = risk;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public BigDecimal getRisk() {
		return risk;
	}
}
