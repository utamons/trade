package com.corn.trade.repository;

import com.corn.trade.entity.CashAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashAccountTypeRepository extends JpaRepository<CashAccountType, Long>, JpaSpecificationExecutor<CashAccountType> {

	CashAccountType findCashAccountTypeByName(String name);
}
