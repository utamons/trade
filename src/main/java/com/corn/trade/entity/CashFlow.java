package com.corn.trade.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "cash_flow")
public class CashFlow {

	public CashFlow() {
	}

	public CashFlow(CashAccount accountFrom,
	                CashAccount accountTo,
	                BigDecimal sumFrom,
	                BigDecimal sumTo,
	                BigDecimal exchangeRate) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
		this.sumFrom = sumFrom;
		this.sumTo = sumTo;
		this.exchangeRate = exchangeRate;
		committedAt = LocalDateTime.now();
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private CashAccount accountFrom;

	@ManyToOne
	private CashAccount accountTo;

	@Column(name = "sum_from", nullable = false)
	private BigDecimal sumFrom;

	@Column(name = "sum_to", nullable = false)
	private BigDecimal sumTo;

	@Column(name = "exchange_rate")
	private BigDecimal exchangeRate;

	@Column(name = "committed_at", nullable = false)
	private LocalDateTime committedAt;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setSumFrom(BigDecimal sumFrom) {
		this.sumFrom = sumFrom;
	}

	public BigDecimal getSumFrom() {
		return sumFrom;
	}

	public void setSumTo(BigDecimal sumTo) {
		this.sumTo = sumTo;
	}

	public BigDecimal getSumTo() {
		return sumTo;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setCommittedAt(LocalDateTime committedAt) {
		this.committedAt = committedAt;
	}

	public LocalDateTime getCommittedAt() {
		return committedAt;
	}

	public CashAccount getAccountFrom() {
		return accountFrom;
	}

	public void setAccountFrom(CashAccount accountFrom) {
		this.accountFrom = accountFrom;
	}

	public CashAccount getAccountTo() {
		return accountTo;
	}

	public void setAccountTo(CashAccount accountTo) {
		this.accountTo = accountTo;
	}
}
