package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public record TradeLogCloseDTO(Long id, Long itemBought, Long itemSold, Double totalBought,  Double totalSold, LocalDateTime dateClose,
                               String note, Double brokerInterest, Double closeCommission,
                               Double finalStopLoss, Double finalTakeProfit)
		implements Serializable {

	@Serial
	private static final long serialVersionUID = -993985822750284913L;

	@JsonCreator
	public TradeLogCloseDTO(
			@JsonProperty("id") Long id,
			@JsonProperty("itemBought") Long itemBought,
			@JsonProperty("itemSold") Long itemSold,
			@JsonProperty("totalBought") Double totalBought,
			@JsonProperty("totalSold") Double totalSold,
			@JsonProperty("dateClose") LocalDateTime dateClose,
			@JsonProperty("note") String note,
			@JsonProperty("brokerInterest") Double brokerInterest,
			@JsonProperty("closeCommission") Double closeCommission,
			@JsonProperty("finalStopLoss") Double finalStopLoss,
			@JsonProperty("finalTakeProfit") Double finalTakeProfit
	) {
		this.id = id;
		this.itemBought = itemBought;
		this.itemSold = itemSold;
		this.totalBought = totalBought;
		this.totalSold = totalSold;
		this.dateClose = dateClose;
		this.note = note;
		this.brokerInterest = brokerInterest;
		this.closeCommission = closeCommission;
		this.finalStopLoss = finalStopLoss;
		this.finalTakeProfit = finalTakeProfit;
	}
}
