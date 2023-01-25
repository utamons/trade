package com.corn.trade.repository;

import com.corn.trade.entity.Broker;
import com.corn.trade.entity.TradeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TradeLogRepository extends JpaRepository<TradeLog, Long>, JpaSpecificationExecutor<TradeLog> {
	Page<TradeLog> findAll(Pageable pageable);

	@Query("select t from TradeLog t where t.broker=:broker and t.dateClose is not null")
	List<TradeLog> findAllClosedByBroker(@Param("broker") Broker broker);

	@Query("select count(t) from TradeLog t where t.broker=:broker  and t.dateClose is null")
	long opensByBroker(Broker broker);
}
