package com.corn.trade.entity;

import com.corn.trade.dto.BrokerDTO;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("unused")
@Entity
@Table(name = "broker")
public class Broker implements Serializable {

	private static final long serialVersionUID = 1612680270274880048L;

	public Broker() {
	}

	public Broker(String name) {
		this.name = name;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

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
