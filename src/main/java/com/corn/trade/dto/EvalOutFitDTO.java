package com.corn.trade.dto;

import java.math.BigDecimal;

import static com.corn.trade.util.Util.round;

@SuppressWarnings("unused")
public class EvalOutFitDTO {

	final BigDecimal fees;
	final BigDecimal breakEven;
	final BigDecimal takeProfit;
	final BigDecimal outcomeExp;
	final BigDecimal stopLoss;
	final BigDecimal price;
	final long items;
	final BigDecimal volume;
	final BigDecimal gainPc;
	final BigDecimal riskPc;
	final BigDecimal risk;
	final BigDecimal riskRewardPc;

	final BigDecimal depositPc;

	public EvalOutFitDTO(Double fees,
	                     Double riskPc,
	                     Double breakEven,
	                     Double takeProfit,
	                     Double outcomeExp,
	                     Double stopLoss,
	                     Double price,
	                     long items,
	                     Double volume,
	                     Double gainPc,
	                     Double risk,
	                     Double riskRewardPc,
	                     Double depositPc) {
		this.fees = round(fees);
		this.breakEven = round(breakEven);
		this.takeProfit = round(takeProfit);
		this.outcomeExp = round(outcomeExp);
		this.stopLoss = round(stopLoss);
		this.price = round(price);
		this.volume = round(volume);
		this.gainPc = round(gainPc);
		this.riskPc = round(riskPc);
		this.risk = round(risk);
		this.riskRewardPc = round(riskRewardPc);
		this.items = items;
		this.depositPc = round(depositPc);
	}

	public BigDecimal getRisk() {
		return risk;
	}

	public BigDecimal getDepositPc() {
		return depositPc;
	}
	public BigDecimal getFees() {
		return fees;
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
		return stopLoss;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public long getItems() {
		return items;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getGainPc() {
		return gainPc;
	}

	public BigDecimal getRiskPc() {
		return riskPc;
	}

	public BigDecimal getRiskRewardPc() {
		return riskRewardPc;
	}
}
