package com.corn.trade.repository;

import com.corn.trade.entity.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long>, JpaSpecificationExecutor<CashFlow> {

}
