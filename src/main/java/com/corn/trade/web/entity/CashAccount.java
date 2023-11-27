package com.corn.trade.web.entity;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "cash_account")
public class CashAccount {

	public CashAccount() {
	}

	public CashAccount(String name, Currency currency, Broker broker, CashAccountType type) {
		this.name = name;
		this.currency = currency;
		this.broker = broker;
		this.type = type;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne
	private Currency currency;

	@ManyToOne
	private Broker broker;

	@ManyToOne
	private CashAccountType type;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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

	public CashAccountType getType() {
		return type;
	}

	public void setType(CashAccountType type) {
		this.type = type;
	}
}
