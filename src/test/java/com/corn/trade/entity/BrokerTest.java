package com.corn.trade.entity;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@Transactional
public class BrokerTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	@Rollback(false)
	public void testCreateBroker() {
		Broker broker = new Broker("Test Broker");
		entityManager.persist(broker);
		entityManager.flush();

		assertThat(broker.getId()).isNotNull();
		assertThat(broker.getName()).isEqualTo("Test Broker");
	}

	@Test
	public void testGetBroker() {
		Broker broker = new Broker("Test Broker");
		entityManager.persist(broker);
		entityManager.flush();

		Broker found = entityManager.find(Broker.class, broker.getId());

		assertThat(found.getId()).isEqualTo(broker.getId());
		assertThat(found.getName()).isEqualTo(broker.getName());
	}

	@Test
	public void testUpdateBroker() {
		Broker broker = new Broker("Test Broker");
		entityManager.persist(broker);
		entityManager.flush();

		broker.setName("Updated Broker");
		entityManager.merge(broker);
		entityManager.flush();

		Broker updated = entityManager.find(Broker.class, broker.getId());

		assertThat(updated.getId()).isEqualTo(broker.getId());
		assertThat(updated.getName()).isEqualTo("Updated Broker");
	}

	@Test
	public void testDeleteBroker() {
		Broker broker = new Broker("Test Broker");
		entityManager.persist(broker);
		entityManager.flush();

		entityManager.remove(broker);
		entityManager.flush();

		Broker deleted = entityManager.find(Broker.class, broker.getId());

		assertThat(deleted).isNull();
	}

}

