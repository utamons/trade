package com.corn.trade.entity;

import com.corn.trade.dto.CurrencyDTO;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("unused")
@Entity
@Table(name = "currency")
public class Currency implements Serializable {

	private static final long serialVersionUID = 2575710149281146318L;
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	public Currency() {}

	public Currency(String name) {
		this.name = name;
	}

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
}
