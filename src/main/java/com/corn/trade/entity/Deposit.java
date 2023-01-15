package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposit")
public class Deposit implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Currency currency;

	@ManyToOne
	private Broker broker;

	@Column(name = "date", nullable = false)
	private LocalDateTime date;

	@Column(name = "sum", nullable = false)
	private BigDecimal sum;

	@Column(name = "total")
	private BigDecimal total;

	@Column(name = "type")
	private String type;

	public Deposit() {}

	public Deposit(Currency currency, Broker broker, LocalDateTime date, BigDecimal sum, BigDecimal total, String type) {
		this.currency = currency;
		this.broker = broker;
		this.date = date;
		this.sum = sum;
		this.total = total;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public BigDecimal getSum() {
		return sum;
	}

	public void setSum(BigDecimal sum) {
		this.sum = sum;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
