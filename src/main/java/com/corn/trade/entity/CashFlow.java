package com.corn.trade.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "cash_flow")
public class CashFlow {

	public CashFlow() {
	}

	public CashFlow(CashAccount accountFrom,
	                CashAccount accountTo,
					TradeLog tradeLog,
	                Double sumFrom,
	                Double sumTo,
	                Double exchangeRate,
	                LocalDateTime committedAt) {
		this.accountFrom = accountFrom;
		this.accountTo = accountTo;
		this.tradeLog = tradeLog;
		this.sumFrom = sumFrom;
		this.sumTo = sumTo;
		this.exchangeRate = exchangeRate;
		this.committedAt = committedAt;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private CashAccount accountFrom;

	@ManyToOne
	private CashAccount accountTo;

	@ManyToOne
	private TradeLog tradeLog;

	@Column(name = "sum_from", nullable = false)
	private Double sumFrom;

	@Column(name = "sum_to", nullable = false)
	private Double sumTo;

	@Column(name = "exchange_rate")
	private Double exchangeRate;

	@Column(name = "committed_at", nullable = false)
	private LocalDateTime committedAt;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setSumFrom(Double sumFrom) {
		this.sumFrom = sumFrom;
	}

	public Double getSumFrom() {
		return sumFrom;
	}

	public void setSumTo(Double sumTo) {
		this.sumTo = sumTo;
	}

	public Double getSumTo() {
		return sumTo;
	}

	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public Double getExchangeRate() {
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

	public TradeLog getTradeLog() {
		return tradeLog;
	}
}
