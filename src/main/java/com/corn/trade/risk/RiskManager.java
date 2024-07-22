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
package com.corn.trade.risk;

import com.corn.trade.entity.PnlEntity;
import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.jpa.PnlRepo;
import com.corn.trade.jpa.TradeRepo;
import com.corn.trade.model.PnL;
import com.corn.trade.type.Stage;
import jakarta.persistence.EntityManager;
import liquibase.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.corn.trade.BaseWindow.*;
import static com.corn.trade.util.Util.fmt;

public class RiskManager {
	private static final Logger log = LoggerFactory.getLogger(RiskManager.class);

	private final PnlRepo             pnlRepo;
	private final TradeRepo           tradeRepo;
	private final Map<String, Double> openRiskMap = new ConcurrentHashMap<>();
	private       double              daily       = 0.0;
	private 	  double              dailyOld    = 0.0;
	private       boolean             canTrade    = false;
	private       String              riskError   = null;

	public RiskManager() throws DatabaseException {
		log.info(
				"RiskManager initialized, MAX_DAILY_LOSS: {}, MAX_WEEKLY_LOSS: {}, MAX_MONTHLY_LOSS: {}, " +
				"MAX_TRADES_PER_DAY: {}",
				MAX_DAILY_LOSS,
				MAX_WEEKLY_LOSS,
				MAX_MONTHLY_LOSS,
				MAX_TRADES_PER_DAY);
		this.pnlRepo = new PnlRepo();
		this.tradeRepo = new TradeRepo();
		initChecks();
	}

	public void updatePnL(PnL pnl) throws DatabaseException {
		daily = pnl.realized() - getOpenRisk();
		if (daily != dailyOld) {
			log.info("Daily PnL updated: {} -> {}, open risk {}", fmt(dailyOld), fmt(daily), fmt(getOpenRisk()));
			dailyOld = daily;
		}
		EntityManager em = JpaUtil.getEntityManager();
		pnlRepo.withEntityManager(em);
		em.getTransaction().begin();
		PnlEntity pnlEntity = pnlRepo.getForDate(LocalDate.now());
		if (pnlEntity == null) {
			pnlEntity = new PnlEntity();
			pnlEntity.setDateAt(LocalDate.now());
			pnlEntity.setValue(daily);
			pnlRepo.save(pnlEntity);
		} else {
			pnlEntity.setValue(daily);
		}
		em.getTransaction().commit();
		pnlRepo.closeEntityManager();
		if (daily <= MAX_DAILY_LOSS) {
			canTrade = false;
			riskError = "Daily loss limit reached";
			log.info("Daily loss limit reached - {}", daily);
		} else if (daily <= MAX_WEEKLY_LOSS && !SIMULATION_MODE) {
			canTrade = false;
			riskError = "Weekly loss limit reached";
		} else if (daily <= MAX_MONTHLY_LOSS && !SIMULATION_MODE) {
			canTrade = false;
			riskError = "Monthly loss limit reached";
		} else {
			canTrade = true;
			riskError = null;
		}
	}

	public void addOpenRisk(String symbol, double openRisk) {
		if (openRiskMap.containsKey(symbol)) {
			return;
		}
		this.openRiskMap.put(symbol, openRisk);
		log.info("Open risk added for: {} {}", symbol, fmt(openRisk));
	}

	public void removeOpenRisk(String symbol) {
		if (!openRiskMap.containsKey(symbol)) {
			log.warn("Open risk not found for: {}", symbol);
			return;
		}
		daily += openRiskMap.get(symbol);
		this.openRiskMap.remove(symbol);
		log.info("Open risk removed for: {}", symbol);
	}

	public double getOpenRisk() {
		return openRiskMap.values().stream().mapToDouble(Double::doubleValue).sum();
	}

	public boolean canTrade(double risk) {
		if (daily - risk < MAX_DAILY_LOSS) {
			riskError = "Daily loss limit reached";
			return false;
		}
		riskError = null;
		return true;
	}

	private void initChecks() throws DatabaseException {
		canTrade = true;
		riskError = null;

		LocalDate today = LocalDate.now();

		LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

		PnlEntity pnlEntity = pnlRepo.getForDate(LocalDate.now());
		if (pnlEntity != null) {
			daily = pnlEntity.getValue();
		}

		if (daily <= MAX_DAILY_LOSS) {
			canTrade = false;
			riskError = "Daily loss limit reached";
			return;
		}

		Double weekly = pnlRepo.countForDateRange(startOfWeek, today);
		if (weekly != null && weekly <= MAX_WEEKLY_LOSS && !SIMULATION_MODE) {
			canTrade = false;
			riskError = "Weekly loss limit reached";
			return;
		}


		Double monthly = pnlRepo.countForDateRange(startOfMonth, today);
		if (monthly != null && monthly <= MAX_MONTHLY_LOSS && !SIMULATION_MODE) {
			canTrade = false;
			riskError = "Monthly loss limit reached";
			return;
		}

		countTrades();
	}


	public boolean canTrade() {
		return STAGE == Stage.DEV || canTrade;
	}

	public String getRiskError() {
		return riskError;
	}

	public void countTrades() {
		long tradeCount = tradeRepo.countClosedForToday();
		if (tradeCount >= MAX_TRADES_PER_DAY) {
			canTrade = false;
			riskError = "Max trades per day reached";
		}
	}
}
