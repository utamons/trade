package com.corn.trade.entity;

import jakarta.persistence.*;

@SuppressWarnings("unused")
@Entity()
public class Ticker implements Comparable<Ticker> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne
	private Exchange exchange;

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

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	@Transient
	public int compareTo(Ticker o) {
		return this.getName().compareTo(o.getName());
	}
}
