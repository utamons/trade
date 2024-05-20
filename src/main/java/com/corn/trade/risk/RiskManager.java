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

import static com.corn.trade.BaseWindow.*;

public class RiskManager {
	private static final Logger log = LoggerFactory.getLogger(RiskManager.class);

	private final PnlRepo   pnlRepo;
	private final TradeRepo tradeRepo;
	private       double    daily      = 0.0;
	private       boolean   canTrade   = false;
	private       String    riskError  = null;

	public RiskManager() throws DatabaseException {
		log.info("RiskManager initialized, MAX_DAILY_LOSS: {}, MAX_WEEKLY_LOSS: {}, MAX_MONTHLY_LOSS: {}, MAX_TRADES_PER_DAY: {}",
				MAX_DAILY_LOSS, MAX_WEEKLY_LOSS, MAX_MONTHLY_LOSS, MAX_TRADES_PER_DAY);
		this.pnlRepo = new PnlRepo();
		this.tradeRepo = new TradeRepo();
		initChecks();
	}

	public void updatePnL(PnL pnl) throws DatabaseException {
		daily = pnl.daily();
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
		}
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
		if (weekly != null && weekly <= MAX_WEEKLY_LOSS) {
			canTrade = false;
			riskError = "Weekly loss limit reached";
			return;
		}


		Double monthly = pnlRepo.countForDateRange(startOfMonth, today);
		if (monthly != null && monthly <= MAX_MONTHLY_LOSS) {
			canTrade = false;
			riskError = "Monthly loss limit reached";
			return;
		}

		countTrades();
	}


	public boolean canTrade() {
		return STAGE == Stage.DEV || SIMULATION_MODE || canTrade;
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
