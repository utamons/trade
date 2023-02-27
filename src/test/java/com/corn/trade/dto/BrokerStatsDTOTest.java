package com.corn.trade.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrokerStatsDTOTest {
    private final static CurrencyDTO USD_CURRENCY = new CurrencyDTO(1L, "USD");
    private final static BrokerDTO     BROKER     = new BrokerDTO(1L, "broker");
    private final static LocalDateTime UPDATED_AT = LocalDateTime.now();

    @Test
    public void testGetAccounts() {
        List<CashAccountDTO> accounts = new ArrayList<>();
        accounts.add(new CashAccountDTO(1L, "Account 1", USD_CURRENCY, BROKER, "type1", 100.0, UPDATED_AT));
        accounts.add(new CashAccountDTO(2L, "Account 2", USD_CURRENCY, BROKER, "type2", 200.0, UPDATED_AT));

        BrokerStatsDTO brokerStatsDTO = new BrokerStatsDTO(accounts, 1000.0, 500.0, 400.0, 5L, 1.0);

        assertEquals(accounts, brokerStatsDTO.getAccounts());
    }

    @Test
    public void testGetOutcome() {
        List<CashAccountDTO> accounts = new ArrayList<>();
        BrokerStatsDTO brokerStatsDTO = new BrokerStatsDTO(accounts, 1000.0, 500.0, 400.0, 5L, 1.0);

        BigDecimal outcome = brokerStatsDTO.getOutcome();

        assertEquals(new BigDecimal("1000.00"), outcome);
    }

    @Test
    public void testGetAvgOutcome() {
        List<CashAccountDTO> accounts = new ArrayList<>();
        BrokerStatsDTO brokerStatsDTO = new BrokerStatsDTO(accounts, 1000.0, 500.0, 400.0, 5L, 1.0);

        BigDecimal avgOutcome = brokerStatsDTO.getAvgOutcome();

        assertEquals(new BigDecimal("500.00"), avgOutcome);
    }

    @Test
    public void testGetAvgProfit() {
        List<CashAccountDTO> accounts = new ArrayList<>();
        BrokerStatsDTO brokerStatsDTO = new BrokerStatsDTO(accounts, 1000.0, 500.0, 400.0, 5L, 1.0);

        BigDecimal avgProfit = brokerStatsDTO.getAvgProfit();

        assertEquals(new BigDecimal("400.00"), avgProfit);
    }

    @Test
    public void testGetOpen() {
        List<CashAccountDTO> accounts = new ArrayList<>();
        BrokerStatsDTO brokerStatsDTO = new BrokerStatsDTO(accounts, 1000.0, 500.0, 400.0, 5L, 1.0);

        long open = brokerStatsDTO.getOpen();

        assertEquals(5L, open);
    }
}

