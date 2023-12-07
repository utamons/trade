package com.corn.trade.trade;

import com.corn.trade.common.Notifier;

import javax.swing.*;

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
	private       boolean powerReserveError       = false;
	private       boolean highDayError            = false;
	private       boolean lowDayError             = false;
	private       boolean tempLevelError          = false;
	private       boolean resistanceError         = false;
	private       boolean supportError            = false;
	private       boolean atrError                = true;
	private       boolean autoUpdate              = false;

	public Levels(JFrame frame) {
		this.frame = frame;
	}

	public void setBestPrice(Double bestPrice) {
		this.bestPrice = bestPrice;
	}

	public Double getPivotPoint() {
		return pivotPoint;
	}

	public void setPowerReserve(Double powerReserve) {
		this.powerReserve = powerReserve;
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

	public Double getTempLevel() {
		return tempLevel;
	}

	public void setTempLevel(Double tempLevel) {
		this.tempLevel = tempLevel;
	}

	public Double getResistance() {
		return resistance;
	}

	public void setResistance(Double resistance) {
		this.resistance = resistance;
	}

	public Double getSupport() {
		return support;
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
		if (tempLevel == null && resistance == null && support == null) {
			tempLevelError = true;
			resistanceError = true;
			supportError = true;
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

	@SuppressWarnings("DuplicatedCode")
	public void reset() {
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
	}

	public String validatePowerReserve() {
		powerReserveError = false;
		String error = null;
		if (powerReserve == null) {
			powerReserveError = true;
			error = "Power reserve must be set\n ";
		} else if (powerReserve <= 0) {
			powerReserveError = true;
			error = "Power reserve must be greater than 0\n ";
		}
		announce();
		return error;
	}

	public void calculatePowerReserve(PositionType positionType) {
		String error = validate();
		if (error == null) {
			error = validatePowerReserve();
		}
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return;
		}
		double techAtr = highDay - lowDay;
		double realAtr = Math.max(techAtr, atr * REALISTIC_POWER_RESERVE);

		if (positionType == PositionType.LONG) {
			powerReserve = realAtr - (pivotPoint - lowDay);
		} else {
			powerReserve = realAtr - (highDay - pivotPoint);
		}
		announce();
	}

	public void calculatePivotPoint(PositionType positionType) {
		pivotPoint = null;
		String error = validate();
		if (error != null) {
			showErrorDlg(frame, error, !autoUpdate);
			announce();
			return;
		}
		if (positionType == PositionType.LONG) {


		}
	}

}
