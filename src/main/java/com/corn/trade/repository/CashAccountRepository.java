package com.corn.trade.repository;

import com.corn.trade.entity.Broker;
import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashAccountType;
import com.corn.trade.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashAccountRepository extends JpaRepository<CashAccount, Long>, JpaSpecificationExecutor<CashAccount> {
	CashAccount findCashAccountByBrokerAndCurrencyAndType(Broker broker, Currency currency, CashAccountType type);
}
