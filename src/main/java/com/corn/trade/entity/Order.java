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
