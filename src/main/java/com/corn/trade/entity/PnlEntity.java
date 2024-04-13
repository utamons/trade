package com.corn.trade.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@SuppressWarnings("unused")
@Entity
@Table(name = "pnl")
public class PnlEntity {
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date_at", nullable = false)
	private LocalDate dateAt;

	@Column(name = "value", nullable = false)
	private double value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDateAt() {
		return dateAt;
	}

	public void setDateAt(LocalDate dateAt) {
		this.dateAt = dateAt;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
