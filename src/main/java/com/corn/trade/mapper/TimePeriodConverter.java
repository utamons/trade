package com.corn.trade.mapper;

import com.corn.trade.service.TimePeriod;
import com.corn.trade.util.Pair;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@SuppressWarnings("unused")
public class TimePeriodConverter {

	private TimePeriodConverter() {
		throw new IllegalStateException("Utility class");
	}

	public static Pair<LocalDateTime, LocalDateTime> getPreviousTimeRange(Pair<LocalDateTime, LocalDateTime> timeRange) {
		LocalDateTime start = timeRange.getLeft();
		LocalDateTime end = timeRange.getRight();
		Duration duration = Duration.between(start, end);

		LocalDateTime newStart = start.minus(duration);
		LocalDateTime newEnd = end.minus(duration);

		return new Pair<>(newStart, newEnd);
	}

	public static Pair<LocalDateTime, LocalDateTime> getNextTimeRange(Pair<LocalDateTime, LocalDateTime> timeRange) {
		LocalDateTime start = timeRange.getLeft();
		LocalDateTime end = timeRange.getRight();
		Duration duration = Duration.between(start, end);

		LocalDateTime newStart = start.plus(duration);
		LocalDateTime newEnd = end.plus(duration);

		return new Pair<>(newStart, newEnd);
	}

	public static Pair<LocalDateTime, LocalDateTime> getDateTimeRange(TimePeriod timePeriod) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start, end;

		end = switch (timePeriod) {
			case CURRENT_WEEK -> {
				start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				yield now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
			}
			case LAST_WEEK -> {
				start = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				yield now.minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
			}
			case CURRENT_MONTH -> {
				start = now.with(TemporalAdjusters.firstDayOfMonth());
				yield now.with(TemporalAdjusters.lastDayOfMonth());
			}
			case LAST_MONTH -> {
				start = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
				yield now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
			}
			case CURRENT_QUARTER -> {
				start = now.with(getQuarterStart());
				yield now.with(getQuarterEnd());
			}
			case LAST_QUARTER -> {
				start = now.minusMonths(3).with(getQuarterStart());
				yield now.minusMonths(3).with(getQuarterEnd());
			}
			case CURRENT_YEAR -> {
				start = now.with(TemporalAdjusters.firstDayOfYear());
				yield now.with(TemporalAdjusters.lastDayOfYear());
			}
			case LAST_YEAR -> {
				start = now.minusYears(1).with(TemporalAdjusters.firstDayOfYear());
				yield now.minusYears(1).with(TemporalAdjusters.lastDayOfYear());
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
