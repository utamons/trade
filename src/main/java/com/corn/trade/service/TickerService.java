package com.corn.trade.service;

import com.corn.trade.entity.Exchange;
import com.corn.trade.entity.Ticker;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.JpaRepo;

import java.util.List;
import java.util.stream.Collectors;

public class TickerService {
	private final JpaRepo<Exchange, Long> exchangeRepo = new JpaRepo<>(Exchange.class);
	private final JpaRepo<Ticker, Long>   tickerRepo   = new JpaRepo<>(Ticker.class);

	public List<Exchange> getExchanges() {
		return exchangeRepo.findAll().stream().sorted().toList();
	}

	public List<Ticker> getTickers() {
		return tickerRepo.findAll().stream().sorted().toList();
	}

	public List<String> getTickerNames() {
		return getTickers().stream().map(Ticker::getName).toList();
	}

	public List<String> getExchangeNames() {
		return getExchanges().stream().map(Exchange::getName).toList();
	}
}
