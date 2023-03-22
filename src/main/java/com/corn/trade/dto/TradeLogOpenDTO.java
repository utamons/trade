package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class TradeLogOpenDTO implements Serializable {

	private static final long serialVersionUID = -4135659016328220866L;

	private final Long id;

	private final String position;

	private final LocalDateTime dateOpen;

	private final Long brokerId;

	private final Long marketId;

	private final Long tickerId;

	private final Long itemNumber;

	private final Double priceOpen;

	private final Double stopLoss;

	private final Double takeProfit;

	private final Double outcomeExpected;

	private final Double risk;

	private final Double breakEven;

	private final Double fees;

	private final String note;

	private final Double levelPrice;

	private final Double atr;

	private final Double goal;

	public Double getGoal() {
		return goal;
	}

	@JsonCreator
	public TradeLogOpenDTO(@JsonProperty("id") Long id,
	                       @JsonProperty("position") String position,
	                       @JsonProperty("dateOpen") LocalDateTime dateOpen,
	                       @JsonProperty("brokerId") Long brokerId,
	                       @JsonProperty("marketId") Long marketId,
	                       @JsonProperty("tickerId") Long tickerId,
	                       @JsonProperty("itemNumber") Long itemNumber,
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
	                       @JsonProperty("goal") Double goal) {
		this.id = id;
		this.position = position;
		this.dateOpen = dateOpen;
		this.brokerId = brokerId;
		this.marketId = marketId;
		this.tickerId = tickerId;
		this.itemNumber = itemNumber;
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
	}

	public String getPosition() {
		return position;
	}

	public LocalDateTime getDateOpen() {
		return dateOpen;
	}

	public Long getBrokerId() {
		return brokerId;
	}

	public Long getMarketId() {
		return marketId;
	}

	public Long getTickerId() {
		return tickerId;
	}

	public Double getBreakEven() {
		return breakEven;
	}

	public Long getItemNumber() {
		return itemNumber;
	}

	public Double getPriceOpen() {
		return priceOpen;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public Double getOutcomeExpected() {
		return outcomeExpected;
	}

	public Double getRisk() {
		return risk;
	}

	public Double getFees() {
		return fees;
	}

	public String getNote() {
		return note;
	}

	public Long getId() {
		return id;
	}

	public Double getLevelPrice() {
		return levelPrice;
	}

	public Double getAtr() {
		return atr;
	}
}
