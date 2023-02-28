package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class TradeLogCloseDTO implements Serializable {

	private static final long serialVersionUID = -993985822750284913L;
	private final Long id;
	private final LocalDateTime dateClose;

	private final Double priceClose;

	private final String note;

	private final Double brokerInterest;

	@JsonCreator
	public TradeLogCloseDTO(@JsonProperty("id") Long id,
	                        @JsonProperty("dateClose") LocalDateTime dateClose,
	                        @JsonProperty("priceClose") Double priceClose,
	                        @JsonProperty("note") String note,
	                        @JsonProperty("brokerInterest") Double brokerInterest) {
		this.id = id;
		this.dateClose = dateClose;
		this.priceClose = priceClose;
		this.note = note;
		this.brokerInterest = brokerInterest;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getDateClose() {
		return dateClose;
	}

	public Double getPriceClose() {
		return priceClose;
	}

	public String getNote() {
		return note;
	}

	public Double getBrokerInterest() {
		return brokerInterest;
	}
}
