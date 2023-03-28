package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class EvalOutFitDTO {

	private final BigDecimal fees;
	private final BigDecimal risk;
	private final BigDecimal breakEven;
	private final BigDecimal takeProfit;
	private final BigDecimal outcomeExp;
	private final Double     stopLoss;

	private final Double price;
	private final long   items;

	public EvalOutFitDTO(BigDecimal fees,
	                     BigDecimal risk,
	                     BigDecimal breakEven,
	                     BigDecimal takeProfit,
	                     BigDecimal outcomeExp,
	                     Double stopLoss,
	                     Double price, long items) {
		this.fees = fees;
		this.risk = risk;
		this.breakEven = breakEven;
		this.takeProfit = takeProfit;
		this.outcomeExp = outcomeExp;
		this.stopLoss = stopLoss;
		this.price = price;
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
		return round(stopLoss);
	}

	public BigDecimal getPrice() {
		return round(price);
	}

	public long getItems() {
		return items;
	}
}
