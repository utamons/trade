package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static com.corn.trade.util.Util.toOutBigDecimal;

@SuppressWarnings("unused")
public class TradeLogDTO implements Serializable {
	private static final long serialVersionUID = 1858149861975993582L;
	private final Long id;

	private final String position;

	private final LocalDateTime dateOpen;

	private final LocalDateTime dateClose;

	private final BrokerDTO broker;

	private final MarketDTO market;

	private final TickerDTO ticker;

	private final CurrencyDTO currency;

	private final Long itemNumber;

	private final Double priceOpen;

	private final Double priceClose;

	private final Double volume;

	private final Double volumeToDeposit;

	private final Double stopLoss;

	private final Double takeProfit;

	private final Double outcomeExpected;

	private final Double risk;

	private final Double breakEven;

	private final Double fees;

	private final Double outcome;

	private final Double outcomePercent;

	private final Double profit;

	private final String note;

	private final String chart;

	private final String grade;

	private final Double goal;

	private final Double brokerInterest;

	private final Long parentId;

	private final Double levelPrice;

	private final Double atr;

	@JsonCreator
	public TradeLogDTO(@JsonProperty("id") Long id,
	                   @JsonProperty("position") String position,
	                   @JsonProperty("dateOpen") LocalDateTime dateOpen,
	                   @JsonProperty("dateClose") LocalDateTime dateClose,
	                   @JsonProperty("broker") BrokerDTO broker,
	                   @JsonProperty("market") MarketDTO market,
	                   @JsonProperty("ticker") TickerDTO ticker,
	                   @JsonProperty("currency") CurrencyDTO currency,
	                   @JsonProperty("itemNumber") Long itemNumber,
	                   @JsonProperty("priceOpen") Double priceOpen,
	                   @JsonProperty("priceClose") Double priceClose,
	                   @JsonProperty("volume") Double volume,
	                   @JsonProperty("volumeToDeposit") Double volumeToDeposit,
	                   @JsonProperty("stopLoss") Double stopLoss,
	                   @JsonProperty("takeProfit") Double takeProfit,
	                   @JsonProperty("outcomeExpected") Double outcomeExpected,
	                   @JsonProperty("risk") Double risk,
	                   @JsonProperty("breakEven") Double breakEven,
	                   @JsonProperty("fees") Double fees,
	                   @JsonProperty("outcome") Double outcome,
	                   @JsonProperty("outcomePercent") Double outcomePercent,
	                   @JsonProperty("profit") Double profit,
	                   @JsonProperty("note") String note,
	                   @JsonProperty("chart") String chart,
	                   @JsonProperty("grade") String grade,
	                   @JsonProperty("goal") Double goal,
	                   @JsonProperty("brokerInterest") Double brokerInterest,
	                   @JsonProperty("parentId") Long parentId,
	                   @JsonProperty("levelPrice") Double levelPrice,
	                   @JsonProperty("atr") Double atr) {
		this.id = id;
		this.position = position;
		this.dateOpen = dateOpen;
		this.dateClose = dateClose;
		this.broker = broker;
		this.market = market;
		this.ticker = ticker;
		this.currency = currency;
		this.itemNumber = itemNumber;
		this.priceOpen = priceOpen;
		this.priceClose = priceClose;
		this.volume = volume;
		this.volumeToDeposit = volumeToDeposit;
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
		this.outcomeExpected = outcomeExpected;
		this.risk = risk;
		this.breakEven = breakEven;
		this.fees = fees;
		this.outcome = outcome;
		this.outcomePercent = outcomePercent;
		this.profit = profit;
		this.note = note;
		this.chart = chart;
		this.grade = grade;
		this.goal = goal;
		this.brokerInterest = brokerInterest;
		this.parentId = parentId;
		this.levelPrice = levelPrice;
		this.atr = atr;
	}

	public Long getId() {
		return id;
	}

	public String getPosition() {
		return position;
	}

	public LocalDateTime getDateOpen() {
		return dateOpen;
	}

	public LocalDateTime getDateClose() {
		return dateClose;
	}

	public BrokerDTO getBroker() {
		return broker;
	}

	public MarketDTO getMarket() {
		return market;
	}

	public TickerDTO getTicker() {
		return ticker;
	}

	public CurrencyDTO getCurrency() {
		return currency;
	}

	public Long getItemNumber() {
		return itemNumber;
	}

	public BigDecimal getPriceOpen() {
		return toOutBigDecimal(priceOpen);
	}

	public BigDecimal getPriceClose() {
		return toOutBigDecimal(priceClose);
	}

	public BigDecimal getVolume() {
		return toOutBigDecimal(volume);
	}

	public BigDecimal getVolumeToDeposit() {
		return toOutBigDecimal(volumeToDeposit);
	}

	public BigDecimal getStopLoss() {
		return toOutBigDecimal(stopLoss);
	}

	public BigDecimal getTakeProfit() {
		return toOutBigDecimal(takeProfit);
	}

	public BigDecimal getOutcomeExpected() {
		return toOutBigDecimal(outcomeExpected);
	}

	public BigDecimal getRisk() {
		return toOutBigDecimal(risk);
	}

	public BigDecimal getFees() {
		return toOutBigDecimal(fees);
	}

	public BigDecimal getOutcome() {
		if (outcome == null)
			return null;
		return BigDecimal.valueOf(outcome).setScale(2, RoundingMode.CEILING);
	}

	@JsonIgnore
	public Double getOutcomeDouble() {
		return outcome;
	}

	public BigDecimal getOutcomePercent() {
		return toOutBigDecimal(outcomePercent);
	}

	public Double getOutcomePercentDouble() {
		return outcomePercent;
	}

	public BigDecimal getProfit() {
		return toOutBigDecimal(profit);
	}

	public String getNote() {
		return note;
	}

	public String getChart() {
		return chart;
	}

	public String getGrade() {
		return grade;
	}

	public BigDecimal getGoal() {
		return toOutBigDecimal(goal);
	}

	public Double getBreakEven() {
		return breakEven;
	}

	public BigDecimal getBrokerInterest() {
		return toOutBigDecimal(brokerInterest);
	}

	public BigDecimal getLevelPrice() {
		return toOutBigDecimal(levelPrice);
	}

	public BigDecimal getAtr() {
		return toOutBigDecimal(atr);
	}

	public Long getParentId() {
		return parentId;
	}
}
