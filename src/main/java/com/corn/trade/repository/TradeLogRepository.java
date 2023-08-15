package com.corn.trade.repository;

import com.corn.trade.dto.CurrencySumDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.TradeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public interface TradeLogRepository extends JpaRepository<TradeLog, Long>, JpaSpecificationExecutor<TradeLog> {
	Page<TradeLog> findAll(Pageable pageable);

	@Query("select t from TradeLog t where t.broker=:broker and t.dateClose is not null")
	List<TradeLog> findAllOpenByBroker(@Param("broker") Broker broker);

	@Query("select count(t) from TradeLog t where t.broker=:broker  and t.dateClose is null")
	long opensCountByBroker(Broker broker);

	@Query("select t from TradeLog t where t.dateClose is not null")
	List<TradeLog> findAllOpen();

	@Query("select new com.corn.trade.dto.CurrencySumDTO(t.currency.id, sum(t.totalBought - t.risk))" +
	       " from TradeLog t where t.position='long' and t.dateClose is null group by t.currency")
	List<CurrencySumDTO> openLongSums();

	@Query("select new com.corn.trade.dto.CurrencySumDTO(t.currency.id, sum(t.risk))" +
	       " from TradeLog t where t.position='short' and t.dateClose is null group by t.currency")
	List<CurrencySumDTO> openShortRisks();

	@Query("select new com.corn.trade.dto.CurrencySumDTO(t.currency.id, sum(t.totalBought - t.risk))" +
	       " from TradeLog t where t.broker=:broker and t.position='long' and t.dateClose is null group by t.currency")
	List<CurrencySumDTO> openLongSumsByBroker(Broker broker);

	@Query("select new com.corn.trade.dto.CurrencySumDTO(t.currency.id, sum(t.risk))" +
	       " from TradeLog t where t.broker=:broker and t.position='short' and t.dateClose is null group by t.currency")
	List<CurrencySumDTO> openShortRisksByBroker(Broker broker);

	@Query("select count(t) from TradeLog t where t.dateOpen between :from and :to")
	long countOpenByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("select count(t) from TradeLog t where t.dateOpen between :from and :to and t.currency.id=:currencyId")
	long countOpenByPeriodAndCurrency(LocalDateTime from, LocalDateTime to, Long currencyId);

	@Query("select count(t) from TradeLog t where t.dateOpen between :from and :to and t.broker.id=:brokerId")
	long countOpenByPeriodAndBroker(LocalDateTime from, LocalDateTime to, Long brokerId);

	@Query("select count(t) from TradeLog t where t.dateOpen between :from and :to and t.currency.id=:currencyId and t.broker.id=:brokerId")
	long countOpenByPeriodAndCurrencyAndBroker(LocalDateTime from, LocalDateTime to, Long currencyId, Long brokerId);
}
