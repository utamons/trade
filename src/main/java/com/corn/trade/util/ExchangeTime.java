/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.util;

import com.corn.trade.entity.Exchange;

import java.time.*;
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
	public boolean withinWeekDays() {
		ZonedDateTime nowInExchangeTimeZone = nowInExchangeTZ();
		return !nowInExchangeTimeZone.getDayOfWeek().equals(DayOfWeek.SATURDAY) &&
		       !nowInExchangeTimeZone.getDayOfWeek().equals(DayOfWeek.SUNDAY);
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

	public LocalDateTime ibkrExecutionToLocalDateTime(String dateTimeString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss z");
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
		return zonedDateTime.toLocalDateTime();
	}
}
