package com.corn.trade.web.repository;

import com.corn.trade.web.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BrokerRepository extends JpaRepository<Broker, Long>, JpaSpecificationExecutor<Broker> {

}
