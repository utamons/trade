package com.corn.trade.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

	private final BigDecimal priceOpen;

	private final BigDecimal priceClose;

	private final BigDecimal volume;

	private final BigDecimal volumeToDeposit;

	private final BigDecimal stopLoss;

	private final BigDecimal takeProfit;

	private final BigDecimal outcomeExpected;

	private final BigDecimal risk;

	private final BigDecimal fees;

	private final BigDecimal outcome;

	private final BigDecimal outcomePercent;

	private final BigDecimal profit;

	private final String note;

	private final String chart;

	private final String grade;

	private final BigDecimal goal;

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
	                   @JsonProperty("priceOpen") BigDecimal priceOpen,
	                   @JsonProperty("priceClose") BigDecimal priceClose,
	                   @JsonProperty("volume") BigDecimal volume,
	                   @JsonProperty("volumeToDeposit") BigDecimal volumeToDeposit,
	                   @JsonProperty("stopLoss") BigDecimal stopLoss,
	                   @JsonProperty("takeProfit") BigDecimal takeProfit,
	                   @JsonProperty("outcomeExpected") BigDecimal outcomeExpected,
	                   @JsonProperty("risk") BigDecimal risk,
	                   @JsonProperty("fees") BigDecimal fees,
	                   @JsonProperty("outcome") BigDecimal outcome,
	                   @JsonProperty("outcomePercent") BigDecimal outcomePercent,
	                   @JsonProperty("profit") BigDecimal profit,
	                   @JsonProperty("note") String note,
	                   @JsonProperty("chart") String chart,
	                   @JsonProperty("grade") String grade,
	                   @JsonProperty("goal") BigDecimal goal) {
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
		this.fees = fees;
		this.outcome = outcome;
		this.outcomePercent = outcomePercent;
		this.profit = profit;
		this.note = note;
		this.chart = chart;
		this.grade = grade;
		this.goal = goal;
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
		return priceOpen;
	}

	public BigDecimal getPriceClose() {
		return priceClose;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public BigDecimal getVolumeToDeposit() {
		return volumeToDeposit;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public BigDecimal getTakeProfit() {
		return takeProfit;
	}

	public BigDecimal getOutcomeExpected() {
		return outcomeExpected;
	}

	public BigDecimal getRisk() {
		return risk;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public BigDecimal getOutcome() {
		return outcome;
	}

	public BigDecimal getOutcomePercent() {
		return outcomePercent;
	}

	public BigDecimal getProfit() {
		return profit;
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
		return goal;
	}
}
