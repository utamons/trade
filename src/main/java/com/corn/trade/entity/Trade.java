package com.corn.trade.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

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

	/**
	 * LONG,SHORT
	 */
	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "quantity", nullable = false)
	private Long quantity;

	@Column(name = "limit_price", nullable = false)
	private BigDecimal limitPrice;

	@Column(name = "stop_loss_price", nullable = false)
	private BigDecimal stopLossPrice;

	@Column(name = "goal", nullable = false)
	private BigDecimal goal;

	@Column(name = "execution_price")
	private BigDecimal executionPrice;

	/**
	 * OPEN, CLOSED, PARTIALLY_CLOSED
	 */
	@Column(name = "status", nullable = false)
	private String status;

	/**
	 * SUCCESS, STOP_LOSS, BE, DROP
	 */
	@Column(name = "result", nullable = false)
	private String result;

	@Column(name = "profit_loss")
	private BigDecimal profitLoss;

	@Column(name = "risk_reward_ratio")
	private BigDecimal riskRewardRatio;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "closed_at")
	private Date closedAt;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public Asset getAsset() {
		return asset;
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

	public void setLimitPrice(BigDecimal limitPrice) {
		this.limitPrice = limitPrice;
	}

	public BigDecimal getLimitPrice() {
		return limitPrice;
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

	public void setExecutionPrice(BigDecimal executionPrice) {
		this.executionPrice = executionPrice;
	}

	public BigDecimal getExecutionPrice() {
		return executionPrice;
	}

	/**
	 * OPEN, CLOSED, PARTIALLY_CLOSED
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * OPEN, CLOSED, PARTIALLY_CLOSED
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

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setClosedAt(Date closedAt) {
		this.closedAt = closedAt;
	}

	public Date getClosedAt() {
		return closedAt;
	}

	@Override
	public String toString() {
		return "Trade{" +
		       "id=" +
		       id +
		       '\'' +
		       "assetId=" + asset +
		       '\'' +
		       "type=" +
		       type +
		       '\'' +
		       "quantity=" +
		       quantity +
		       '\'' +
		       "limitPrice=" +
		       limitPrice +
		       '\'' +
		       "stopLossPrice=" +
		       stopLossPrice +
		       '\'' +
		       "goal=" +
		       goal +
		       '\'' +
		       "executionPrice=" +
		       executionPrice +
		       '\'' +
		       "status=" +
		       status +
		       '\'' +
		       "result=" +
		       result +
		       '\'' +
		       "profitLoss=" +
		       profitLoss +
		       '\'' +
		       "riskRewardRatio=" +
		       riskRewardRatio +
		       '\'' +
		       "createdAt=" +
		       createdAt +
		       '\'' +
		       "closedAt=" +
		       closedAt +
		       '\'' +
		       '}';
	}
}
