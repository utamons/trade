package com.corn.trade.repository;

import com.corn.trade.entity.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TickerRepository extends JpaRepository<Ticker, Long>, JpaSpecificationExecutor<Ticker> {

}
