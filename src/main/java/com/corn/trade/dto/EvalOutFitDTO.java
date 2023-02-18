package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.toOutBigDecimal;

public class EvalOutFitDTO {

	private final BigDecimal fees;
	private final BigDecimal     risk;
	private final BigDecimal     breakEven;
	private final BigDecimal     takeProfit;
	private final BigDecimal     outcomeExp;
	private final Double     stopLoss;
	private final long       items;

	public EvalOutFitDTO(BigDecimal fees,
	                     BigDecimal risk,
	                     BigDecimal breakEven,
	                     BigDecimal takeProfit,
	                     BigDecimal outcomeExp,
	                     Double stopLoss,
	                     long items) {
		this.fees = fees;
		this.risk = risk;
		this.breakEven = breakEven;
		this.takeProfit = takeProfit;
		this.outcomeExp = outcomeExp;
		this.stopLoss = stopLoss;
		this.items = items;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public BigDecimal getRisk() {
		return risk;
	}

	public BigDecimal getBreakEven() {
		return breakEven;
	}

	public BigDecimal getTakeProfit() {
		return takeProfit;
	}

	public BigDecimal getOutcomeExp() {
		return outcomeExp;
	}

	public BigDecimal getStopLoss() {
		return toOutBigDecimal(stopLoss);
	}

	public long getItems() {
		return items;
	}
}
