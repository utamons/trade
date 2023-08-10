package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
@Entity
@Table(name = "market")
public class Market implements Serializable {

	@Serial
	private static final long serialVersionUID = 8172595950495106639L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "timezone", nullable = false)
	private Integer timezone;

	public Market() {}

	public Market(String name) {
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

	public Integer getTimezone() {
		return timezone;
	}

	public void setTimezone(Integer timezone) {
		this.timezone = timezone;
	}
}
