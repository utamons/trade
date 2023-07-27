package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
public record TradeLogCloseDTO(Long id, Integer quantity, LocalDateTime dateClose, Double priceClose, String note,
                               Double brokerInterest, Double totalSold, Double fees,
                               Double totalBought) implements Serializable {

	@Serial
	private static final long serialVersionUID = -993985822750284913L;

	@JsonCreator
	public TradeLogCloseDTO(@JsonProperty("id") Long id,
	                        @JsonProperty("quantity") Integer quantity,
	                        @JsonProperty("dateClose") LocalDateTime dateClose,
	                        @JsonProperty("priceClose") Double priceClose,
	                        @JsonProperty("note") String note,
	                        @JsonProperty("brokerInterest") Double brokerInterest,
	                        @JsonProperty("totalSold") Double totalSold,
	                        @JsonProperty("fees") Double fees,
	                        @JsonProperty("totalBought") Double totalBought

	) {
		this.id = id;
		this.quantity = quantity;
		this.dateClose = dateClose;
		this.priceClose = priceClose;
		this.note = note;
		this.brokerInterest = brokerInterest;
		this.totalSold = totalSold;
		this.fees = fees;
		this.totalBought = totalBought;
	}
}
