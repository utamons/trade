package com.corn.trade.repository;

import com.corn.trade.entity.Broker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BrokerRepository extends JpaRepository<Broker, Long>, JpaSpecificationExecutor<Broker> {

}
