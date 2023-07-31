package com.corn.trade.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CashAccountTest {

	@PersistenceContext
	private EntityManager entityManager;

	private Currency        currency;
	private Broker          broker;
	private CashAccountType accountType;

	@BeforeEach
	public void setup() {
		currency = entityManager.find(Currency.class, 1L);
		broker   = new Broker("Broker A", currency);
		entityManager.persist(broker);
		accountType = entityManager.find(CashAccountType.class, 1L);
		entityManager.flush();
		entityManager.createQuery("DELETE FROM CashAccount").executeUpdate();
		entityManager.flush();
	}

	@Test
	public void testCreateCashAccount() {
		String      accountName = "Account 1";
		CashAccount cashAccount = new CashAccount(accountName, currency, broker, accountType);
		entityManager.persist(cashAccount);
		entityManager.flush();

		Optional<CashAccount> found = entityManager
				.createQuery("SELECT c FROM CashAccount c WHERE c.name = :accountName", CashAccount.class)
				.setParameter("accountName", accountName)
				.getResultList()
				.stream()
				.findFirst();

		assertThat(found.isPresent()).isTrue();
		assertThat(found.get().getCurrency().getId()).isEqualTo(currency.getId());
		assertThat(found.get().getBroker().getId()).isEqualTo(broker.getId());
		assertThat(found.get().getType().getId()).isEqualTo(accountType.getId());
	}

	@Test
	public void testUpdateCashAccount() {
		String      accountName = "Account 2";
		CashAccount cashAccount = new CashAccount(accountName, currency, broker, accountType);
		entityManager.persist(cashAccount);
		entityManager.flush();

		Optional<CashAccount> found = entityManager
				.createQuery("SELECT c FROM CashAccount c WHERE c.name = :accountName", CashAccount.class)
				.setParameter("accountName", accountName)
				.getResultList()
				.stream()
				.findFirst();

		assertThat(found.isPresent()).isTrue();

		entityManager.persist(found.get());
		entityManager.flush();

		Optional<CashAccount> updated = entityManager
				.createQuery("SELECT c FROM CashAccount c WHERE c.name = :accountName", CashAccount.class)
				.setParameter("accountName", accountName)
				.getResultList()
				.stream()
				.findFirst();

		assertThat(updated.isPresent()).isTrue();
	}
}

