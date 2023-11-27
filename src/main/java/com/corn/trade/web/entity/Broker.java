package com.corn.trade.web.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * Broker entity
 */
@SuppressWarnings("unused")
@Entity
@Table(name = "broker")
public class Broker implements Serializable {

	@Serial
	private static final long serialVersionUID = 1612680270274880048L;

	public Broker() {
	}

	public Broker(String name, Currency feeCurrency) {
		this.name = name;
		this.feeCurrency = feeCurrency;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Name of the broker
	 */
	@Column(name = "name", nullable = false)
	private String name;

	/**
	 * Currency used for fees to the broker
	 */
	@ManyToOne
	private Currency feeCurrency;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Currency getFeeCurrency() {
		return feeCurrency;
	}

	public void setFeeCurrency(Currency feeCurrency) {
		this.feeCurrency = feeCurrency;
	}
}
