package com.corn.trade.mapper;

import com.corn.trade.service.TimePeriod;
import com.corn.trade.util.Generated;
import com.corn.trade.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class TimePeriodConverter {

	@Generated
	private TimePeriodConverter() {
		throw new IllegalStateException("Utility class");
	}

	public static List<LocalDateTime> getWeekdaysBetween(LocalDateTime from, LocalDateTime to) {
		List<LocalDateTime> weekdayStartDates = new ArrayList<>();

		LocalDateTime currentDateTime = from;

		while (currentDateTime.isBefore(to) || currentDateTime.isEqual(to)) {
			DayOfWeek dayOfWeek = currentDateTime.getDayOfWeek();

			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
				weekdayStartDates.add(currentDateTime.with(LocalTime.MIN));
			}

			currentDateTime = currentDateTime.plusDays(1);
		}

		return weekdayStartDates;
	}

	public static Pair<LocalDateTime, LocalDateTime> getPreviousTimeRange(Pair<LocalDateTime, LocalDateTime> timeRange) {
		LocalDateTime start = timeRange.left();
		LocalDateTime end = timeRange.right();
		Duration duration = Duration.between(start, end);

		LocalDateTime newStart = start.minus(duration);
		LocalDateTime newEnd = end.minus(duration);

		return new Pair<>(newStart, newEnd);
	}

	public static Pair<LocalDateTime, LocalDateTime> getNextTimeRange(Pair<LocalDateTime, LocalDateTime> timeRange) {
		LocalDateTime start = timeRange.left();
		LocalDateTime end = timeRange.right();
		Duration duration = Duration.between(start, end);

		LocalDateTime newStart = start.plus(duration);
		LocalDateTime newEnd = end.plus(duration);

		return new Pair<>(newStart, newEnd);
	}

	public static Pair<LocalDateTime, LocalDateTime> getDateTimeRange(TimePeriod timePeriod) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start, end;

		end = switch (timePeriod) {
			case ALL_TIME -> {
				start = LocalDateTime.of(2023, 1, 1, 0, 0);
				yield now;
			}
			case WEEK_TO_DATE -> {
				start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
				yield now;
			}
			case LAST_WEEK -> {
				start = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
				yield now.minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
			}
			case MONTH_TO_DATE -> {
				start = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
				yield now;
			}
			case LAST_MONTH -> {
				start = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
				yield now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
			}
			case QUARTER_TO_DATE -> {
				start = now.with(getQuarterStart()).with(LocalTime.MIN);
				yield now;
			}
			case LAST_QUARTER -> {
				start = now.minusMonths(3).with(getQuarterStart()).with(LocalTime.MIN);
				yield now.minusMonths(3).with(getQuarterEnd()).with(LocalTime.MAX);
			}
			case YEAR_TO_DATE -> {
				start = now.with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
				yield now;
			}
			case LAST_YEAR -> {
				start = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
				yield now.minusYears(1).with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
			}
			default -> throw new IllegalArgumentException("Unsupported TimePeriod: " + timePeriod);
		};

		return new Pair<>(start, end);
	}

	private static TemporalAdjuster getQuarterStart() {
		return temporal -> {
			int currentMonth = temporal.get(ChronoField.MONTH_OF_YEAR);
			int monthOfFirstQuarter = ((currentMonth - 1) / 3) * 3 + 1;
			return temporal.with(ChronoField.MONTH_OF_YEAR, monthOfFirstQuarter)
			               .with(TemporalAdjusters.firstDayOfMonth());
		};
	}

	private static TemporalAdjuster getQuarterEnd() {
		return temporal -> {
			int currentMonth = temporal.get(ChronoField.MONTH_OF_YEAR);
			int monthOfLastQuarter = ((currentMonth - 1) / 3 + 1) * 3;
			return temporal.with(ChronoField.MONTH_OF_YEAR, monthOfLastQuarter)
			               .with(TemporalAdjusters.lastDayOfMonth());
		};
	}
}
