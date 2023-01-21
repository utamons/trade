package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class TradeLogCloseDTO implements Serializable {

	private static final long serialVersionUID = -993985822750284913L;
	private final Long id;
	private final LocalDateTime dateClose;

	private final BigDecimal priceClose;

	private final BigDecimal fees;

	private final String note;


	@JsonCreator
	public TradeLogCloseDTO(@JsonProperty("id") Long id,
	                        @JsonProperty("dateClose") LocalDateTime dateClose,
	                        @JsonProperty("priceClose") BigDecimal priceClose,
	                        @JsonProperty("fees") BigDecimal fees,
	                        @JsonProperty("note") String note) {
		this.id = id;
		this.dateClose = dateClose;
		this.priceClose = priceClose;
		this.fees = fees;
		this.note = note;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getDateClose() {
		return dateClose;
	}

	public BigDecimal getPriceClose() {
		return priceClose;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public String getNote() {
		return note;
	}
}
