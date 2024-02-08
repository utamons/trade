package com.corn.trade.entity;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity()
@Table(name = "exchange")
public class Exchange implements Comparable<Exchange>  {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne
	private Currency currency;

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

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	@Override
	@Transient
	public int compareTo(Exchange o) {
		return this.getName().compareTo(o.getName());
	}
}
