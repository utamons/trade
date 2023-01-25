package com.corn.trade.repository;

import com.corn.trade.entity.Broker;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CashAccountRepository extends JpaRepository<CashAccount, Long>, JpaSpecificationExecutor<CashAccount> {
	CashAccount findCashAccountByBrokerAndCurrencyAndType(Broker broker, Currency currency, CashAccountType type);

	List<CashAccount> findAllByType(CashAccountType type);

	List<CashAccount> findAllByBrokerAndType(Broker broker, CashAccountType type);
}
