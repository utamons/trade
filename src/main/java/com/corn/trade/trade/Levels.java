package com.corn.trade.trade;

public class Levels {
	@SuppressWarnings("FieldCanBeLocal")
	private final Double REALISTIC_POWER_RESERVE = 0.8;
	private       Double atr;
	private Double tempLevel;
	private Double resistance;
	private Double support;
	private Double  highDay;
	private Double  lowDay;
	private Double  powerReserve;
	private boolean powerReserveError = false;
	private boolean highDayError = false;
	private boolean lowDayError = false;
	private boolean tempLevelError = false;
	private boolean resistanceError = false;
	private boolean supportError = false;
	private boolean atrError= true;

	public boolean isPowerReserveError() {
		return powerReserveError;
	}

	public Double getPowerReserve() {
		return powerReserve;
	}

	public void setPowerReserve(Double powerReserve) {
		this.powerReserve = powerReserve;
	}

	public boolean isAtrError() {
		return atrError;
	}

	public Double getAtr() {
		return atr;
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

	public Double getHighDay() {
		return highDay;
	}

	public void setHighDay(Double highDay) {
		this.highDay = highDay;
	}

	public Double getLowDay() {
		return lowDay;
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

		if (atr == null) {
			atrError = true;
			return "atr must be set\n ";
		}
		if (atr <= 0) {
			atrError = true;
			return "atr must be greater than 0\n ";
		}
		if (lowDay == null) {
			lowDayError = true;
			return "lowDay must be set\n ";
		}
		if (highDay == null) {
			highDayError = true;
			return "highDay must be set\n ";
		}
		if (tempLevel == null && resistance == null && support == null) {
			tempLevelError = true;
			resistanceError = true;
			supportError = true;
			return "at lease one of levels must be set\n ";
		}
		if (lowDay <= 0) {
			lowDayError = true;
			return "lowDay must be greater than 0\n ";
		}
		if (highDay <= 0) {
			highDayError = true;
			return "highDay must be greater than 0\n ";
		}

		if (lowDay >= highDay) {
			lowDayError = true;
			return "lowDay must be less than highDay\n ";
		}

		// support validation
		if (support != null) {
			if (support <= 0) {
				supportError = true;
				return "Support must be greater than 0\n ";
			}
			if (support <= lowDay || support >= highDay) {
				supportError = true;
				return "Support must be between lowDay and highDay\n ";
			}
			if (resistance != null && support >= resistance) {
				supportError = true;
				return "Support must be less than resistance";
			}
		}

		// resistance validation
		if (resistance != null) {
			if (resistance <= 0) {
				resistanceError = true;
				return "Resistance must be greater than 0\n ";
			}
			if (resistance <= lowDay || resistance >= highDay) {
				resistanceError = true;
				return "Resistance must be between lowDay and highDay\n ";
			}
		}

		// tempLevel validation
		if (tempLevel != null) {
			if (tempLevel <= 0) {
				tempLevelError = true;
				return "Temp. level must be greater than 0\n ";
			}
			if (tempLevel <= lowDay || tempLevel >= highDay) {
				tempLevelError = true;
				return "Temp. level must be between lowDay and highDay\n ";
			}
			if (resistance != null && tempLevel >= resistance) {
				tempLevelError = true;
				return "Temp. level must be less than resistance";
			} else if (support != null && tempLevel <= support) {
				tempLevelError = true;
				return "Temp. level must be greater than support";
			}
		}

		return null;
	}

	public String validatePowerReserve() {
		powerReserveError = false;
		if (powerReserve == null) {
			powerReserveError = true;
			return "Power reserve must be set\n ";
		}
		if (powerReserve <= 0) {
			powerReserveError = true;
			return "Power reserve must be greater than 0\n ";
		}
		return null;
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

	public void calculatePowerReserve(PositionType positionType) {
		double techAtr = highDay - lowDay;
		double realAtr = Math.max(techAtr, atr * REALISTIC_POWER_RESERVE);

		if (positionType == PositionType.LONG) {
			powerReserve = realAtr - (tempLevel - lowDay);
		} else {
			powerReserve = realAtr - (highDay - tempLevel);
		}
	}
}
