package com.corn.trade.repository;

import com.corn.trade.entity.Currency;
import com.corn.trade.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long>, JpaSpecificationExecutor<CurrencyRate> {

	CurrencyRate findRateByCurrencyAndDate(Currency currency, LocalDate date);
}
