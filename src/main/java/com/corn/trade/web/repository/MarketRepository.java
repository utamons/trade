package com.corn.trade.web.repository;

import com.corn.trade.web.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MarketRepository extends JpaRepository<Market, Long>, JpaSpecificationExecutor<Market> {

}
