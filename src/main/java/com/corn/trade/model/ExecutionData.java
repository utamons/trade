package com.corn.trade.model;

import com.ib.client.Execution;

import java.time.LocalDateTime;
import java.util.List;

public class ExecutionData {
	private final String        orderId;
	private final String        assetName;
	private final LocalDateTime time;
	private final Double        price;
	private final Double        avgPrice;
	private final long        quantity;

	public ExecutionData(String orderId,
	                     String assetName,
	                     LocalDateTime time,
	                     Double price,
	                     Double avgPrice,
	                     long quantity) {
		this.orderId = orderId;
		this.assetName = assetName;
		this.time = time;
		this.price = price;
		this.avgPrice = avgPrice;
		this.quantity = quantity;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getAssetName() {
		return assetName;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public Double getPrice() {
		return price;
	}

	public Double getAvgPrice() {
		return avgPrice;
	}

	public long getQuantity() {
		return quantity;
	}
}
