package com.corn.trade.entity;

import com.corn.trade.dto.TickerDTO;

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

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "short_name", nullable = false)
	private String shortName;

	@ManyToOne
	private Currency currency;

	public Ticker() {}

	public Ticker(String name, String shortName, Currency currency) {
		this.name = name;
		this.shortName = shortName;
		this.currency = currency;
	}

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

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
}
