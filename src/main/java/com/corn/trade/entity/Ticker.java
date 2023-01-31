package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("unused")
@Entity
@Table(name = "ticker")
public class Ticker implements Serializable {

	private static final long serialVersionUID = -7416079741753396618L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "long_name", nullable = false)
	private String longName;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne
	private Currency currency;

	public Ticker() {}

	public Ticker(String longName, String name, Currency currency) {
		this.longName = longName;
		this.name = name;
		this.currency = currency;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setLongName(String name) {
		this.longName = name;
	}

	public String getLongName() {
		return longName;
	}

	public void setName(String shortName) {
		this.name = shortName;
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
}
