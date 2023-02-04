package com.corn.trade.entity;

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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
