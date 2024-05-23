/*
	Trade
	Copyright (C) 2024  Cornknight

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * internal broker id for the order
	 */
	@Column(name = "order_id")
	private String orderId;

	@ManyToOne
	private Trade trade;

	/**
	 * MAIN, STOP_LOSS, TAKE_PROFIT, DROP_ALL
	 */
	@Column(name = "role", nullable = false)
	private String role;

	/**
	 * STP, LMT, STP LMT etc.
	 */
	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "quantity", nullable = false)
	private Long quantity;

	/**
	 * i.e. limit price
	 */
	@Column(name = "stop_price")
	private BigDecimal stopPrice;

	/**
	 * i.e stop price
	 */
	@Column(name = "aux_price")
	private BigDecimal auxPrice;

	@Column(name = "avg_price")
	private BigDecimal avgPrice;

	/**
	 * ACTIVE, EXECUTED
	 */
	@Column(name = "status")
	private String status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "executed_at")
	private LocalDateTime executedAt;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	/**
	 * internal broker id for the order
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * internal broker id for the order
	 */
	public String getOrderId() {
		return orderId;
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

	/**
	 * MAIN, STOP_LOSS, TAKE_PROFIT, DROP_ALL
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * MAIN, STOP_LOSS, TAKE_PROFIT, DROP_ALL
	 */
	public String getRole() {
		return role;
	}

	/**
	 * STP, LMT, STP LMT etc.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * STP, LMT, STP LMT etc.
	 */
	public String getType() {
		return type;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Long getQuantity() {
		return quantity;
	}

	/**
	 * i.e. limit price
	 */
	public void setStopPrice(BigDecimal price) {
		this.stopPrice = price;
	}

	/**
	 * i.e. limit price
	 */
	public BigDecimal getStopPrice() {
		return stopPrice;
	}

	/**
	 * i.e stop price
	 */
	public void setAuxPrice(BigDecimal auxPrice) {
		this.auxPrice = auxPrice;
	}

	/**
	 * i.e stop price
	 */
	public BigDecimal getAuxPrice() {
		return auxPrice;
	}

	public void setAvgPrice(BigDecimal avgPrice) {
		this.avgPrice = avgPrice;
	}

	public BigDecimal getAvgPrice() {
		return avgPrice;
	}

	/**
	 * ACTIVE, EXECUTED
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * ACTIVE, EXECUTED
	 */
	public String getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getExecutedAt() {
		return executedAt;
	}

	public void setExecutedAt(LocalDateTime executedAt) {
		this.executedAt = executedAt;
	}
}
