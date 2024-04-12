package com.corn.trade.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@Entity
@Table(name = "trade")
public class Trade {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Asset asset;

	@OneToMany(mappedBy = "trade")
	private List<Order> orders;

	/**
	 * LONG,SHORT
	 */
	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "quantity", nullable = false)
	private Long quantity;

	@Column(name = "initial_price", nullable = false)
	private BigDecimal initialPrice;

	@Column(name = "stop_loss_price", nullable = false)
	private BigDecimal stopLossPrice;

	@Column(name = "goal", nullable = false)
	private BigDecimal goal;

	/**
	 * NEW, OPEN, CLOSED, PARTIALLY_CLOSED
	 */
	@Column(name = "status", nullable = false)
	private String status;

	/**
	 * SUCCESS, STOP_LOSS, BE, DROP
	 */
	@Column(name = "result")
	private String result;

	@Column(name = "profit_loss")
	private BigDecimal profitLoss;

	@Column(name = "risk_reward_ratio")
	private BigDecimal riskRewardRatio;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "closed_at")
	private LocalDateTime closedAt;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Asset getAsset() {
		return asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	/**
	 * LONG,SHORT
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * LONG,SHORT
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

	public void setInitialPrice(BigDecimal initialPrice) {
		this.initialPrice = initialPrice;
	}

	public BigDecimal getInitialPrice() {
		return initialPrice;
	}

	public void setStopLossPrice(BigDecimal stopLossPrice) {
		this.stopLossPrice = stopLossPrice;
	}

	public BigDecimal getStopLossPrice() {
		return stopLossPrice;
	}

	public void setGoal(BigDecimal goal) {
		this.goal = goal;
	}

	public BigDecimal getGoal() {
		return goal;
	}

	/**
	 * NEW, OPEN, CLOSED, PARTIALLY_CLOSED
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * NEW, OPEN, CLOSED, PARTIALLY_CLOSED
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * SUCCESS, STOP_LOSS, BE, DROP
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * SUCCESS, STOP_LOSS, BE, DROP
	 */
	public String getResult() {
		return result;
	}

	public void setProfitLoss(BigDecimal profitLoss) {
		this.profitLoss = profitLoss;
	}

	public BigDecimal getProfitLoss() {
		return profitLoss;
	}

	public void setRiskRewardRatio(BigDecimal riskRewardRatio) {
		this.riskRewardRatio = riskRewardRatio;
	}

	public BigDecimal getRiskRewardRatio() {
		return riskRewardRatio;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.closedAt = closedAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}
}
