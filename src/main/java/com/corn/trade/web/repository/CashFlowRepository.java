package com.corn.trade.web.repository;

import com.corn.trade.web.entity.CashAccount;
import com.corn.trade.web.entity.CashFlow;
import com.corn.trade.web.entity.TradeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long>, JpaSpecificationExecutor<CashFlow> {

	@Query("select sum(cf.sumTo) from CashFlow cf where cf.accountTo = ?1")
	Double getSumToByAccount(CashAccount accountTo);

	@Query("select sum(cf.sumTo) from CashFlow cf where cf.accountTo = ?1 and cf.committedAt <= ?2")
	Double getSumToByAccountToDate(CashAccount accountTo, LocalDateTime date);

	@Query("select sum(cf.sumFrom) from CashFlow cf where cf.accountFrom = ?1")
	Double getSumFromByAccount(CashAccount accountFrom);

	@Query("select sum(cf.sumFrom) from CashFlow cf where cf.accountFrom = ?1 and cf.committedAt <= ?2")
	Double getSumFromByAccountToDate(CashAccount accountFrom, LocalDateTime date);

	@Query("select sum(cf.sumFrom - cf.sumTo) from CashFlow cf")
	Double getCashFlowBalance();

	CashFlow findCashFlowByTradeLog(TradeLog tradeLog);
}
