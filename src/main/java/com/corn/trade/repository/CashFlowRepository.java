package com.corn.trade.repository;

import com.corn.trade.entity.CashAccount;
import com.corn.trade.entity.CashFlow;
import com.corn.trade.entity.TradeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long>, JpaSpecificationExecutor<CashFlow> {

	@Query("select sum(cf.sumTo) from CashFlow cf where cf.accountTo = ?1")
	Double getSumToByAccount(CashAccount accountTo);

	@Query("select sum(cf.sumFrom) from CashFlow cf where cf.accountFrom = ?1")
	Double getSumFromByAccount(CashAccount accountFrom);

	CashFlow findCashFlowByTradeLog(TradeLog tradeLog);
}
