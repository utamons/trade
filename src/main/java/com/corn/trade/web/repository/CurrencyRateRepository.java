package com.corn.trade.web.repository;

import com.corn.trade.web.entity.Currency;
import com.corn.trade.web.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long>, JpaSpecificationExecutor<CurrencyRate> {

	CurrencyRate findRateByCurrencyAndDate(Currency currency, LocalDate date);
}
