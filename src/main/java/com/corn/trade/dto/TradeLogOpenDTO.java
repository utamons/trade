package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public record TradeLogOpenDTO(

		// Basic data
		String position, LocalDateTime dateOpen, Long brokerId, Long marketId, Long tickerId,

		// Estimated data

		Double estimatedPriceOpen, Double estimatedFees, Double estimatedBreakEven,
		Long estimatedItems, Double riskToCapitalPc, Double risk, Double levelPrice, Double atr,

		// Real data
		Double openStopLoss, Double openTakeProfit, Long itemBought, Long itemSold,
		Double totalBought, Double totalSold, Double openCommission,
		String note) implements Serializable {

	@Serial
	private static final long serialVersionUID = -4135659016328220866L;


	@JsonCreator
	public TradeLogOpenDTO(
			@JsonProperty("position") String position,
			@JsonProperty("dateOpen") LocalDateTime dateOpen,
			@JsonProperty("brokerId") Long brokerId,
			@JsonProperty("marketId") Long marketId,
			@JsonProperty("tickerId") Long tickerId,
			@JsonProperty("estimatedPriceOpen") Double estimatedPriceOpen,
			@JsonProperty("estimatedFees") Double estimatedFees,
			@JsonProperty("estimatedBreakEven") Double estimatedBreakEven,
			@JsonProperty("estimatedItems") Long estimatedItems,
			@JsonProperty("riskToCapitalPc") Double riskToCapitalPc,
			@JsonProperty("risk") Double risk,
			@JsonProperty("price") Double levelPrice,
			@JsonProperty("atr") Double atr,
			@JsonProperty("openStopLoss") Double openStopLoss,
			@JsonProperty("openTakeProfit") Double openTakeProfit,
			@JsonProperty("itemBought") Long itemBought,
			@JsonProperty("itemSold") Long itemSold,
			@JsonProperty("totalBought") Double totalBought,
			@JsonProperty("totalSold") Double totalSold,
			@JsonProperty("openCommission") Double openCommission,
			@JsonProperty("note") String note) {
		this.position = position;
		this.dateOpen = dateOpen;
		this.brokerId = brokerId;
		this.marketId = marketId;
		this.tickerId = tickerId;
		this.estimatedPriceOpen = estimatedPriceOpen;
		this.estimatedFees = estimatedFees;
		this.estimatedBreakEven = estimatedBreakEven;
		this.estimatedItems = estimatedItems;
		this.riskToCapitalPc = riskToCapitalPc;
		this.risk = risk;
		this.levelPrice = levelPrice;
		this.atr = atr;
		this.openStopLoss = openStopLoss;
		this.openTakeProfit = openTakeProfit;
		this.itemBought = itemBought;
		this.itemSold = itemSold;
		this.totalBought = totalBought;
		this.totalSold = totalSold;
		this.openCommission = openCommission;
		this.note = note;
	}


}
