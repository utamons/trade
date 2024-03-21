package com.corn.trade.util;

import com.corn.trade.entity.Exchange;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ExchangeTime {
	private final LocalTime startTrading;
	private final LocalTime endTrading;
	private final String timeZone;

	public ExchangeTime(Exchange exchange) {
		String tradingHours = exchange.getTradingHours();
		timeZone = exchange.getTimeZone();

		String[]  hoursParts   = tradingHours.split("-");
		startTrading = LocalTime.parse(hoursParts[0], DateTimeFormatter.ofPattern("HH:mm"));
		endTrading   = LocalTime.parse(hoursParts[1], DateTimeFormatter.ofPattern("HH:mm"));
	}

	public ZonedDateTime nowInExchangeTZ() {
		return ZonedDateTime.now(ZoneId.of(timeZone));
	}

	public String getNowTime(String timeFormat) {
		return nowInExchangeTZ().format(DateTimeFormatter.ofPattern(timeFormat));
	}
	public boolean withinTradingHours() {
		ZonedDateTime nowInExchangeTimeZone = nowInExchangeTZ();
		LocalTime now = nowInExchangeTimeZone.toLocalTime();
		return now.isAfter(startTrading) && now.isBefore(endTrading);
	}

	public boolean afterTradingHours() {
		ZonedDateTime nowInExchangeTimeZone = nowInExchangeTZ();
		LocalTime now = nowInExchangeTimeZone.toLocalTime();
		return now.isAfter(endTrading);
	}

	public ZonedDateTime lastTradingDayEnd() {
		return afterTradingHours() ? nowInExchangeTZ() : nowInExchangeTZ().minusDays(1);
	}

	public LocalTime endTrading() {
		return endTrading;
	}
}
