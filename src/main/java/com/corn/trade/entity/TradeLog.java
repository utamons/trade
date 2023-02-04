package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "trade_log")
public class TradeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "position", nullable = false)
	private String position;

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
	private Double priceOpen;

	@Column(name = "price_close")
	private Double priceClose;

	@Column(name = "volume", nullable = false)
	private Double volume;

	@Column(name = "volume_to_deposit")
	private Double volumeToDeposit;

	@Column(name = "stop_loss")
	private Double stopLoss;

	@Column(name = "take_profit")
	private Double takeProfit;

	@Column(name = "outcome_expected")
	private Double outcomeExpected;

	@Column(name = "risk")
	private Double risk;

	@Column(name = "fees", nullable = false)
	private Double fees;

	@Column(name = "break_even", nullable = false)
	private Double breakEven;

	@Column(name = "outcome")
	private Double outcome;

	@Column(name = "outcome_percent")
	private Double outcomePercent;

	@Column(name = "profit")
	private Double profit;

	@Column(name = "note")
	private String note;

	@Column(name = "grade")
	private String grade;

	@Column(name = "goal")
	private Double goal;

	@Column(name = "chart")
	private byte[] chart;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
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

	public Double getPriceOpen() {
		return priceOpen;
	}

	public void setPriceOpen(Double priceOpen) {
		this.priceOpen = priceOpen;
	}

	public Double getPriceClose() {
		return priceClose;
	}

	public void setPriceClose(Double priceClose) {
		this.priceClose = priceClose;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Double getVolumeToDeposit() {
		return volumeToDeposit;
	}

	public void setVolumeToDeposit(Double volumeToDeposit) {
		this.volumeToDeposit = volumeToDeposit;
	}

	public Double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public Double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(Double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public Double getOutcomeExpected() {
		return outcomeExpected;
	}

	public void setOutcomeExpected(Double outcomeExpected) {
		this.outcomeExpected = outcomeExpected;
	}

	public Double getRisk() {
		return risk;
	}

	public void setRisk(Double risk) {
		this.risk = risk;
	}

	public Double getFees() {
		return fees;
	}

	public void setFees(Double fees) {
		this.fees = fees;
	}

	public Double getOutcome() {
		return outcome;
	}

	public void setOutcome(Double outcome) {
		this.outcome = outcome;
	}

	public Double getOutcomePercent() {
		return outcomePercent;
	}

	public void setOutcomePercent(Double outcomePercent) {
		this.outcomePercent = outcomePercent;
	}

	public Double getProfit() {
		return profit;
	}

	public void setProfit(Double profit) {
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

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public Double getGoal() {
		return goal;
	}

	public void setGoal(Double goal) {
		this.goal = goal;
	}

	public Double getBreakEven() {
		return breakEven;
	}

	public void setBreakEven(Double breakEven) {
		this.breakEven = breakEven;
	}
}
