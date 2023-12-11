package com.corn.trade.trade;

import com.corn.trade.common.Notifier;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.corn.trade.util.Util.showErrorDlg;

public class Levels extends Notifier {
	@SuppressWarnings("FieldCanBeLocal")
	private final Double  REALISTIC_POWER_RESERVE = 0.8;
	private final JFrame  frame;
	private       Double  atr;
	private       Double  tempLevel;
	private       Double  resistance;
	private       Double  support;
	private       Double  highDay;
	private       Double  lowDay;
	private       Double  powerReserve;
	private       Double  bestPrice;
	private       Double  pivotPoint;
	private       boolean pivotPointTempLevel;
	private       boolean pivotPointResistance;
	private       boolean pivotPointSupport;
	private       boolean pivotPointBestPrice;
	private       boolean powerReserveError       = false;
	private       boolean highDayError            = false;
	private       boolean lowDayError             = false;
	private       boolean tempLevelError          = false;
	private       boolean resistanceError         = false;
	private       boolean supportError            = false;
	private       boolean atrError                = true;
	private       boolean autoUpdate              = false;
	private       boolean bestPriceError          = false;

	public boolean isPivotPointTempLevel() {
		return pivotPointTempLevel;
	}

	public boolean isPivotPointResistance() {
		return pivotPointResistance;
	}

	public boolean isPivotPointSupport() {
		return pivotPointSupport;
	}

	public boolean isPivotPointBestPrice() {
		return pivotPointBestPrice;
	}

	public Levels(JFrame frame) {
		this.frame = frame;
	}

	public void setBestPrice(Double bestPrice) {
		this.bestPrice = bestPrice;
	}

	public Double getPivotPoint() {
		return pivotPoint;
	}

	private void resetPivotFlags() {
		pivotPointTempLevel = false;
		pivotPointResistance = false;
		pivotPointSupport = false;
		pivotPointBestPrice = false;
	}


	private void setPivotPointFlag() {
		resetPivotFlags();
		if (pivotPoint != null) {
			if (pivotPoint.equals(tempLevel)) {
				pivotPointTempLevel = true;
			} else if (pivotPoint.equals(resistance)) {
				pivotPointResistance = true;
			} else if (pivotPoint.equals(support)) {
				pivotPointSupport = true;
			} else if (pivotPoint.equals(bestPrice)) {
				pivotPointBestPrice = true;
			}
		}
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public boolean isPowerReserveError() {
		return powerReserveError;
	}

	public Double getPowerReserve() {
		return powerReserve;
	}

	public void setPowerReserve(Double powerReserve) {
		this.powerReserve = powerReserve;
	}

	public boolean isBestPriceError() {
		return bestPriceError;
	}

	public boolean isAtrError() {
		return atrError;
	}

	public void setAtr(Double atr) {
		this.atr = atr;
	}

	public boolean isHighDayError() {
		return highDayError;
	}

	public boolean isLowDayError() {
		return lowDayError;
	}

	public void setHighDay(Double highDay) {
		this.highDay = highDay;
	}

	public void setLowDay(Double lowDay) {
		this.lowDay = lowDay;
	}

	public boolean isTempLevelError() {
		return tempLevelError;
	}

	public boolean isResistanceError() {
		return resistanceError;
	}

	public boolean isSupportError() {
		return supportError;
	}

	public void setTempLevel(Double tempLevel) {
		this.tempLevel = tempLevel;
	}

	public void setResistance(Double resistance) {
		this.resistance = resistance;
	}

	public void setSupport(Double support) {
		this.support = support;
	}

	public String validate() {
		lowDayError = false;
		highDayError = false;
		tempLevelError = false;
		resistanceError = false;
		supportError = false;
		atrError = false;
		bestPriceError = false;
		String error = null;

		if (atr == null) {
			atrError = true;
			error = "atr must be set\n ";
		} else if (atr <= 0) {
			atrError = true;
			error = "atr must be greater than 0\n ";
		}
		if (lowDay == null) {
			lowDayError = true;
			error = "lowDay must be set\n ";
		} else if (lowDay <= 0) {
			lowDayError = true;
			error = "lowDay must be greater than 0\n ";
		}
		if (highDay == null) {
			highDayError = true;
			error = "highDay must be set\n ";
		} else if (highDay <= 0) {
			highDayError = true;
			error = "highDay must be greater than 0\n ";
		}
		if (tempLevel == null && resistance == null && support == null && bestPrice == null) {
			tempLevelError = true;
			resistanceError = true;
			supportError = true;
			bestPriceError = true;
			error = "at lease one of levels must be set\n ";
		}

		if (highDay != null && lowDay != null && lowDay >= highDay) {
			lowDayError = true;
			error = "lowDay must be less than highDay\n ";
		}

		// support validation
		if (support != null) {
			if (support <= 0) {
				supportError = true;
				error = "Support must be greater than 0\n ";
			}
			if (lowDay != null && highDay != null && (support <= lowDay || support >= highDay)) {
				supportError = true;
				error = "Support must be between lowDay and highDay\n ";
			}
			if (resistance != null && support >= resistance) {
				supportError = true;
				error = "Support must be less than resistance";
			}
		}

		// resistance validation
		if (resistance != null) {
			if (resistance <= 0) {
				resistanceError = true;
				error = "Resistance must be greater than 0\n ";
			}
			if (lowDay != null && highDay != null && (resistance <= lowDay || resistance >= highDay)) {
				resistanceError = true;
				error = "Resistance must be between lowDay and highDay\n ";
			}
		}

		// tempLevel validation
		if (tempLevel != null) {
			if (tempLevel <= 0) {
				tempLevelError = true;
				error = "Temp. level must be greater than 0\n ";
			}
			if (tempLevel <= lowDay || tempLevel >= highDay) {
				tempLevelError = true;
				error = "Temp. level must be between lowDay and highDay\n ";
			}
			if (resistance != null && tempLevel >= resistance) {
				tempLevelError = true;
				error = "Temp. level must be less than resistance";
			} else if (support != null && tempLevel <= support) {
				tempLevelError = true;
				error = "Temp. level must be greater than support";
			}
		}
		announce();
		return error;
	}

	public boolean isStopLossUnderLevels(double stopLoss, PositionType positionType) {
		// Find the closest level to pivotPoint based on positionType
		Double closestLevel = null;

		if (positionType == PositionType.LONG) {
			closestLevel = Stream.of(tempLevel, resistance, support)
			                     .filter(Objects::nonNull)
			                     .filter(level -> level > stopLoss)
			                     .min(Comparator.naturalOrder())
			                     .orElse(null);
		} else if (positionType == PositionType.SHORT) {
			closestLevel = Stream.of(tempLevel, resistance, support)
			                     .filter(Objects::nonNull)
			                     .filter(level -> level < stopLoss)
			                     .max(Comparator.naturalOrder())
			                     .orElse(null);
		}

		if (closestLevel != null && closestLevel.equals(pivotPoint) ) {
			return true;
		}
		if (closestLevel != null) {
			if (positionType == PositionType.LONG) {
				return pivotPoint > closestLevel;
			} else {
				return pivotPoint < closestLevel;
			}
		}

		return false; // Return false if no valid level was found
	}


	@SuppressWarnings("DuplicatedCode")
	public void reset() {
		bestPrice = null;
		atr = null;
		tempLevel = null;
		resistance = null;
		support = null;
		highDay = null;
		lowDay = null;
		highDayError = false;
		lowDayError = false;
		tempLevelError = false;
		resistanceError = false;
		supportError = false;
		atrError = false;
		powerReserve = null;
		powerReserveError = false;
		resetPivotFlags();
		announce();
	}

	public void calculatePowerReserve(PositionType positionType) {
		String error = validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return;
		}

		double techAtr = highDay - lowDay;
		double realAtr = Math.max(techAtr, atr * REALISTIC_POWER_RESERVE);

		// Assuming pivotPoint, support, and resistance are already calculated/set before this method is called
		if (positionType == PositionType.LONG && resistance != null && resistance > pivotPoint) {
			powerReserve = Math.abs(pivotPoint - resistance);
		} else if (positionType == PositionType.SHORT && support != null && support < pivotPoint) {
			powerReserve = Math.abs(pivotPoint - support);
		} else {
			// Original calculation if no levels are defined
			if (positionType == PositionType.LONG) { // No levels above pivot point
				powerReserve = realAtr - (pivotPoint - lowDay);
			} else { // No levels below pivot point
				powerReserve = realAtr - (highDay - pivotPoint);
			}
		}
		announce();
	}

	public void calculatePivotPoint(PositionType positionType) {
		pivotPoint = null;
		Double pivot = null;
		String error = validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return;
		}
		List<Double> definedLevels = Stream.of(tempLevel, resistance, support)
		                                   .filter(Objects::nonNull)
		                                   .toList();

		if (definedLevels.isEmpty()) {
			pivot = bestPrice;
		} else if (bestPrice != null) {
			Double closestLevel = definedLevels.stream()
			                                   .min(Comparator.comparingDouble(level -> Math.abs(level - bestPrice)))
			                                   .orElseThrow(() -> new IllegalArgumentException("No closest level found"));

			if (positionType == PositionType.LONG) {
				pivot = bestPrice >= closestLevel ? bestPrice : closestLevel;
			} else {
				pivot = bestPrice <= closestLevel ? bestPrice : closestLevel;
			}
		} else {
			switch (definedLevels.size()) {
				case 1:
					pivot = definedLevels.get(0);
					break;
				case 2:
				case 3:
					if (positionType == PositionType.LONG) {
						pivot = Collections.min(definedLevels);
					} else {
						pivot = Collections.max(definedLevels);
					}
					break;
				default:
					break; // All cases are covered, no default action required
			}
		}

		this.pivotPoint = pivot; // Set the calculated pivot point
		setPivotPointFlag();
		announce(); // Announce update
	}

	public Double getBestPrice() {
		return bestPrice;
	}

	public Double getAtr() {
		return atr;
	}

	public Double getHighDay() {
		return highDay;
	}

	public Double getLowDay() {
		return lowDay;
	}
}
