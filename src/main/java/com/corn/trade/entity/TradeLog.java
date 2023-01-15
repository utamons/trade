package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_log")
public class TradeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date_open", nullable = false)
	private LocalDateTime dateOpen;

	@Column(name = "date_close")
	private LocalDateTime dateClose;

	@ManyToOne
	private Broker broker;

	@ManyToOne
	private Market market;

	@ManyToOne
	private Ticker ticker;

	@ManyToOne
	private Currency currency;

	@Column(name = "item_number", nullable = false)
	private Long itemNumber;

	@Column(name = "price_open", nullable = false)
	private BigDecimal priceOpen;

	@Column(name = "price_close")
	private BigDecimal priceClose;

	@Column(name = "volume", nullable = false)
	private BigDecimal volume;

	@Column(name = "volume_to_deposit")
	private BigDecimal volumeToDeposit;

	@Column(name = "stop_loss")
	private BigDecimal stopLoss;

	@Column(name = "take_profit")
	private BigDecimal takeProfit;

	@Column(name = "outcome_expected")
	private BigDecimal outcomeExpected;

	@Column(name = "risk")
	private BigDecimal risk;

	@Column(name = "fees", nullable = false)
	private BigDecimal fees;

	@Column(name = "outcome")
	private BigDecimal outcome;

	@Column(name = "outcome_percent")
	private BigDecimal outcomePercent;

	@Column(name = "profit")
	private BigDecimal profit;

	@Column(name = "note")
	private String note;

	@Column(name = "chart")
	private byte[] chart;

	public TradeLog() {}

	public TradeLog(LocalDateTime dateOpen,
	                LocalDateTime dateClose,
	                Broker broker,
	                Market market,
	                Ticker ticker,
	                Currency currency,
	                Long itemNumber,
	                BigDecimal priceOpen,
	                BigDecimal priceClose,
	                BigDecimal volume,
	                BigDecimal volumeToDeposit,
	                BigDecimal stopLoss,
	                BigDecimal takeProfit,
	                BigDecimal outcomeExpected,
	                BigDecimal risk,
	                BigDecimal fees,
	                BigDecimal outcome,
	                BigDecimal outcomePercent,
	                BigDecimal profit,
	                String note,
	                byte[] chart) {
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
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDateOpen() {
		return dateOpen;
	}

	public void setDateOpen(LocalDateTime dateOpen) {
		this.dateOpen = dateOpen;
	}

	public LocalDateTime getDateClose() {
		return dateClose;
	}

	public void setDateClose(LocalDateTime dateClose) {
		this.dateClose = dateClose;
	}

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public Ticker getTicker() {
		return ticker;
	}

	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Long getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(Long itemNumber) {
		this.itemNumber = itemNumber;
	}

	public BigDecimal getPriceOpen() {
		return priceOpen;
	}

	public void setPriceOpen(BigDecimal priceOpen) {
		this.priceOpen = priceOpen;
	}

	public BigDecimal getPriceClose() {
		return priceClose;
	}

	public void setPriceClose(BigDecimal priceClose) {
		this.priceClose = priceClose;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public BigDecimal getVolumeToDeposit() {
		return volumeToDeposit;
	}

	public void setVolumeToDeposit(BigDecimal volumeToDeposit) {
		this.volumeToDeposit = volumeToDeposit;
	}

	public BigDecimal getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(BigDecimal stopLoss) {
		this.stopLoss = stopLoss;
	}

	public BigDecimal getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(BigDecimal takeProfit) {
		this.takeProfit = takeProfit;
	}

	public BigDecimal getOutcomeExpected() {
		return outcomeExpected;
	}

	public void setOutcomeExpected(BigDecimal outcomeExpected) {
		this.outcomeExpected = outcomeExpected;
	}

	public BigDecimal getRisk() {
		return risk;
	}

	public void setRisk(BigDecimal risk) {
		this.risk = risk;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public void setFees(BigDecimal fees) {
		this.fees = fees;
	}

	public BigDecimal getOutcome() {
		return outcome;
	}

	public void setOutcome(BigDecimal outcome) {
		this.outcome = outcome;
	}

	public BigDecimal getOutcomePercent() {
		return outcomePercent;
	}

	public void setOutcomePercent(BigDecimal outcomePercent) {
		this.outcomePercent = outcomePercent;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public byte[] getChart() {
		return chart;
	}

	public void setChart(byte[] chart) {
		this.chart = chart;
	}
}
