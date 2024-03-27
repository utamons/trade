package com.corn.trade.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@SuppressWarnings("unused")
@Entity
@Table(name = "order")
public class Order implements Serializable {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * internal broker id for the order
	 */
	@Column(name = "order_id", nullable = false)
	private String orderId;

	@ManyToOne
	private Asset asset;

	@ManyToOne
	private Trade trade;

	@ManyToOne
	private Order parentOrder;

	@Column(name = "position_type", nullable = false)
	private String positionType;

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
	@Column(name = "price", nullable = false)
	private BigDecimal price;

	/**
	 * i.e stop price
	 */
	@Column(name = "aux_price")
	private BigDecimal auxPrice;

	/**
	 * average fill price
	 */
	@Column(name = "avg_fill_price")
	private BigDecimal avgFillPrice;

	/**
	 * NEW, FILLED, CANCELLED, REJECTED, INACTIVE
	 */
	@Column(name = "status", nullable = false)
	private String status;

	/**
	 * DAY, GTC, IOC, FOK
	 */
	@Column(name = "time_in_force")
	private String timeInForce;

	/**
	 * number of filled positions
	 */
	@Column(name = "filled")
	private Long filled;

	/**
	 * number of remaining positions
	 */
	@Column(name = "remaining")
	private Long remaining;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "submitted_at")
	private Date submittedAt;

	@Column(name = "filled_at")
	private Date filledAt;

	@Column(name = "cancelled_at")
	private Date cancelledAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "errorCode")
	private String errorCode;

	@Column(name = "errorMsg")
	private String errorMsg;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Order getParentOrder() {
		return parentOrder;
	}

	public void setParentOrder(Order parentOrder) {
		this.parentOrder = parentOrder;
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

	public Asset getAsset() {
		return asset;
	}

	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}

	public String getPositionType() {
		return positionType;
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
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	/**
	 * i.e. limit price
	 */
	public BigDecimal getPrice() {
		return price;
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

	/**
	 * average fill price
	 */
	public void setAvgFillPrice(BigDecimal avgFillPrice) {
		this.avgFillPrice = avgFillPrice;
	}

	/**
	 * average fill price
	 */
	public BigDecimal getAvgFillPrice() {
		return avgFillPrice;
	}

	/**
	 * NEW, FILLED, CANCELLED, REJECTED, INACTIVE
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * NEW, FILLED, CANCELLED, REJECTED, INACTIVE
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * DAY, GTC, IOC, FOK
	 */
	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}

	/**
	 * DAY, GTC, IOC, FOK
	 */
	public String getTimeInForce() {
		return timeInForce;
	}

	/**
	 * number of filled positions
	 */
	public void setFilled(Long filled) {
		this.filled = filled;
	}

	/**
	 * number of filled positions
	 */
	public Long getFilled() {
		return filled;
	}

	/**
	 * number of remaining positions
	 */
	public void setRemaining(Long remaining) {
		this.remaining = remaining;
	}

	/**
	 * number of remaining positions
	 */
	public Long getRemaining() {
		return remaining;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setSubmittedAt(Date submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Date getSubmittedAt() {
		return submittedAt;
	}

	public void setFilledAt(Date filledAt) {
		this.filledAt = filledAt;
	}

	public Date getFilledAt() {
		return filledAt;
	}

	public void setCancelledAt(Date cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public Date getCancelledAt() {
		return cancelledAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	@Override
	public String toString() {
		return "Order{" +
		       "id=" +
		       id +
		       '\'' +
		       "orderId=" +
		       orderId +
		       '\'' +
		       "assetId=" + asset +
		       '\'' +
		       "tradeId=" + trade +
		       '\'' +
		       "positionType=" +
		       positionType +
		       '\'' +
		       "role=" +
		       role +
		       '\'' +
		       "type=" +
		       type +
		       '\'' +
		       "quantity=" +
		       quantity +
		       '\'' +
		       "price=" +
		       price +
		       '\'' +
		       "auxPrice=" +
		       auxPrice +
		       '\'' +
		       "avgFillPrice=" +
		       avgFillPrice +
		       '\'' +
		       "status=" +
		       status +
		       '\'' +
		       "timeInForce=" +
		       timeInForce +
		       '\'' +
		       "filled=" +
		       filled +
		       '\'' +
		       "remaining=" +
		       remaining +
		       '\'' +
		       "createdAt=" +
		       createdAt +
		       '\'' +
		       "submittedAt=" +
		       submittedAt +
		       '\'' +
		       "filledAt=" +
		       filledAt +
		       '\'' +
		       "cancelledAt=" +
		       cancelledAt +
		       '\'' +
		       "updatedAt=" +
		       updatedAt +
		       '\'' +
		       "errorCode=" +
		       errorCode +
		       '\'' +
		       "errorMsg=" +
		       errorMsg +
		       '\'' +
		       '}';
	}
}
