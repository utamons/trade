package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class TradeLogOpenDTO implements Serializable {

	private static final long serialVersionUID = -4135659016328220866L;
	private final String position;

	private final LocalDateTime dateOpen;

	private final Long brokerId;

	private final Long marketId;

	private final Long tickerId;

	private final Long itemNumber;

	private final BigDecimal priceOpen;

	private final BigDecimal stopLoss;

	private final BigDecimal takeProfit;

	private final BigDecimal outcomeExpected;

	private final BigDecimal risk;

	private final BigDecimal fees;

	private final String note;

	@JsonCreator
	public TradeLogOpenDTO(@JsonProperty("position") String position,
	                       @JsonProperty("dateOpen") LocalDateTime dateOpen,
	                       @JsonProperty("brokerId") Long brokerId,
	                       @JsonProperty("marketId") Long marketId,
	                       @JsonProperty("tickerId") Long tickerId,
	                       @JsonProperty("itemNumber") Long itemNumber,
	                       @JsonProperty("priceOpen") BigDecimal priceOpen,
	                       @JsonProperty("stopLoss") BigDecimal stopLoss,
	                       @JsonProperty("takeProfit") BigDecimal takeProfit,
	                       @JsonProperty("outcomeExpected") BigDecimal outcomeExpected,
	                       @JsonProperty("risk") BigDecimal risk,
	                       @JsonProperty("fees") BigDecimal fees,
	                       @JsonProperty("note") String note) {
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
		this.fees = fees;
		this.note = note;
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

	public Long getItemNumber() {
		return itemNumber;
	}

	public BigDecimal getPriceOpen() {
		return priceOpen;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public BigDecimal getTakeProfit() {
		return takeProfit;
	}

	public BigDecimal getOutcomeExpected() {
		return outcomeExpected;
	}

	public BigDecimal getRisk() {
		return risk;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public String getNote() {
		return note;
	}
}
