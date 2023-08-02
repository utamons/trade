package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public record TradeLogOpenDTO(Long id, String position, LocalDateTime dateOpen, Long brokerId, Long marketId, Long tickerId,
                              Long estimatedItems, Long realItems, Double priceOpen, Double stopLoss, Double takeProfit, Double outcomeExpected,
                              Double risk, Double breakEven, Double fees, String note, Double levelPrice, Double atr,
                              Double goal, Double totalBought, Double totalSold) implements Serializable {

	@Serial
	private static final long serialVersionUID = -4135659016328220866L;


	@JsonCreator
	public TradeLogOpenDTO(@JsonProperty("id") Long id,
	                       @JsonProperty("position") String position,
	                       @JsonProperty("dateOpen") LocalDateTime dateOpen,
	                       @JsonProperty("brokerId") Long brokerId,
	                       @JsonProperty("marketId") Long marketId,
	                       @JsonProperty("tickerId") Long tickerId,
	                       @JsonProperty("estimatedItems") Long estimatedItems,
	                       @JsonProperty("realItems") Long realItems,
	                       @JsonProperty("priceOpen") Double priceOpen,
	                       @JsonProperty("stopLoss") Double stopLoss,
	                       @JsonProperty("takeProfit") Double takeProfit,
	                       @JsonProperty("outcomeExpected") Double outcomeExpected,
	                       @JsonProperty("risk") Double risk,
	                       @JsonProperty("breakEven") Double breakEven,
	                       @JsonProperty("fees") Double fees,
	                       @JsonProperty("note") String note,
	                       @JsonProperty("levelPrice") Double levelPrice,
	                       @JsonProperty("atr") Double atr,
	                       @JsonProperty("goal") Double goal,
                           @JsonProperty("totalBought") Double totalBought,
                           @JsonProperty("totalSold") Double totalSold
	) {
		this.id = id;
		this.position = position;
		this.dateOpen = dateOpen;
		this.brokerId = brokerId;
		this.marketId = marketId;
		this.tickerId = tickerId;
		this.estimatedItems = estimatedItems;
		this.priceOpen = priceOpen;
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
		this.outcomeExpected = outcomeExpected;
		this.risk = risk;
		this.breakEven = breakEven;
		this.fees = fees;
		this.note = note;
		this.levelPrice = levelPrice;
		this.atr = atr;
		this.goal = goal;
		this.totalBought = totalBought;
		this.totalSold = totalSold;
		this.realItems = realItems;
	}


}
