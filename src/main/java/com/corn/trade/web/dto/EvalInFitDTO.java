package com.corn.trade.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record EvalInFitDTO(Long brokerId, Long tickerId, Double levelPrice, Double atr, Double riskPc, Double riskRewardPc,
                           Double depositPc, Double stopLoss, LocalDate date, boolean isShort, boolean technicalStop) {
	@JsonCreator
	public EvalInFitDTO(
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("levelPrice") Double levelPrice,
			@JsonProperty("atr") Double atr,
			@JsonProperty("riskPc") Double riskPc,
			@JsonProperty("riskRewardPc") Double riskRewardPc,
			@JsonProperty("depositPc") Double depositPc,
			@JsonProperty("stopLoss") Double stopLoss,
			@JsonProperty("date") LocalDate date,
			@JsonProperty("short") boolean isShort,
			@JsonProperty("technicalStop") boolean technicalStop) {
		this.brokerId = brokerId;
		this.tickerId = tickerId;
		this.atr = atr;
		this.depositPc = depositPc;
		this.levelPrice = levelPrice;
		this.riskRewardPc = riskRewardPc;
		this.riskPc = riskPc;
		this.stopLoss = stopLoss;
		this.date = date;
		this.isShort = isShort;
		this.technicalStop = technicalStop;
	}
}
