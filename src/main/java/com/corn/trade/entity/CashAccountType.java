package com.corn.trade.entity;

import javax.persistence.*;

@SuppressWarnings("unused")
@Entity
@Table(name = "cash_account_type")
public class CashAccountType {

	public CashAccountType() {
	}

	public CashAccountType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
