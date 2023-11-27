package com.corn.trade.web.dto;

import com.corn.trade.web.util.Util;

import java.math.BigDecimal;

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
		this.fees = Util.round(fees);
		this.breakEven = Util.round(breakEven);
		this.takeProfit = Util.round(takeProfit);
		this.outcomeExp = Util.round(outcomeExp);
		this.stopLoss = Util.round(stopLoss);
		this.price = Util.round(price);
		this.volume = Util.round(volume);
		this.gainPc = Util.round(gainPc);
		this.riskPc = Util.round(riskPc);
		this.risk = Util.round(risk);
		this.riskRewardPc = Util.round(riskRewardPc);
		this.items = items;
		this.depositPc = Util.round(depositPc);
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
