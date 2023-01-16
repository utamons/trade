package com.corn.trade.repository;

import com.corn.trade.entity.Broker;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepositRepository extends JpaRepository<Deposit, Long>, JpaSpecificationExecutor<Deposit> {

	@Query("select max(d.id) from Deposit d where d.currency=:currency and d.broker=:broker order by d.date desc")
	Long getMaxId(@Param("currency")Currency currency, @Param("broker")Broker broker);

}
