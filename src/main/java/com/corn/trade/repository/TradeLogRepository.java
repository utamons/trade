package com.corn.trade.repository;

import com.corn.trade.entity.TradeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TradeLogRepository extends JpaRepository<TradeLog, Long>, JpaSpecificationExecutor<TradeLog> {
	Page<TradeLog> findAll(Pageable pageable);
}
