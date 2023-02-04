package com.corn.trade.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_rate")
@Cacheable(false)
public class CurrencyRate {

	public CurrencyRate() {
	}

	public CurrencyRate(LocalDate date, Currency currency, Double rate) {
		this.date = date;
		this.currency = currency;
		this.rate = rate;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date", nullable = false)
	private LocalDate date;

	@ManyToOne
	private Currency currency;

	@Column(name = "rate", nullable = false)
	private Double rate;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Double getRate() {
		return rate;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
}
