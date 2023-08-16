package com.corn.trade.entity;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "trade_log")
public class TradeLog implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/*
	 * Basic data *********************************************************************************************************
	 */

	/**
	 * Long or Short
	 */
	@Column(name = "position", nullable = false)
	private String position;

	@Column(name = "date_open", nullable = false)
	private LocalDateTime dateOpen;

	@Column(name = "date_close")
	private LocalDateTime dateClose;

	@ManyToOne(optional = false)
	private Broker broker;

	@ManyToOne(optional = false)
	private Market market;

	@ManyToOne(optional = false)
	private Ticker ticker;

	@ManyToOne(optional = false)
	private Currency currency;

	/*
	 * Estimated data *********************************************************************************************************
	 */

	@Column(name = "estimated_price_open", nullable = false)
	private Double estimatedPriceOpen;

	@Column(name = "estimated_fees", nullable = false)
	private Double estimatedFees;

	@Column(name = "estimated_break_even", nullable = false)
	private Double estimatedBreakEven;

	@Column(name = "estimated_items", nullable = false)
	private Long estimatedItems;

	@Column(name = "risk_to_capital_pc", nullable = false)
	private Double riskToCapitalPc;

	@Column(name = "risk", nullable = false)
	private Double risk;

	@Column(name="level_price")
	private Double levelPrice;

	@Column(name="atr")
	private Double atr;

	/*
	 * Real trade data *********************************************************************************************************
	 */

	@Column(name = "open_stop_loss", nullable = false)
	private Double openStopLoss;

	@Column(name = "open_take_profit", nullable = false)
	private Double openTakeProfit;

	@Column(name="broker_interest")
	private Double brokerInterest;

	@Column(name="total_bought")
	private Double totalBought;

	@Column(name="total_sold")
	private Double totalSold;

	@Column(name = "item_bought")
	private Long itemBought;

	@Column(name = "item_sold")
	private Long itemSold;

	@Column(name="final_stop_loss")
	private Double finalStopLoss;

	@Column(name="final_take_profit")
	private Double finalTakeProfit;

	@Column(name="open_commission", nullable = false)
	private Double openCommission;

	@Column(name="close_commission")
	private Double closeCommission;

	@Column(name="parts_closed")
	private Long partsClosed;

	@Column(name = "note")
	private String note;

	/*
	 * Getters and Setters *********************************************************************************************************
	 */

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

	public Double getEstimatedPriceOpen() {
		return estimatedPriceOpen;
	}

	public void setEstimatedPriceOpen(Double estimatedPriceOpen) {
		this.estimatedPriceOpen = estimatedPriceOpen;
	}

	public Double getEstimatedFees() {
		return estimatedFees;
	}

	public void setEstimatedFees(Double estimatedFees) {
		this.estimatedFees = estimatedFees;
	}

	public Double getEstimatedBreakEven() {
		return estimatedBreakEven;
	}

	public void setEstimatedBreakEven(Double estimatedBreakEven) {
		this.estimatedBreakEven = estimatedBreakEven;
	}

	public Double getRiskToCapitalPc() {
		return riskToCapitalPc;
	}

	public void setRiskToCapitalPc(Double riskToCapitalPc) {
		this.riskToCapitalPc = riskToCapitalPc;
	}

	public Double getLevelPrice() {
		return levelPrice;
	}

	public void setLevelPrice(Double levelPrice) {
		this.levelPrice = levelPrice;
	}

	public Double getAtr() {
		return atr;
	}

	public void setAtr(Double atr) {
		this.atr = atr;
	}

	public Double getOpenStopLoss() {
		return openStopLoss;
	}

	public void setOpenStopLoss(Double openStopLoss) {
		this.openStopLoss = openStopLoss;
	}

	public Double getOpenTakeProfit() {
		return openTakeProfit;
	}

	public void setOpenTakeProfit(Double openTakeProfit) {
		this.openTakeProfit = openTakeProfit;
	}

	public Double getBrokerInterest() {
		return brokerInterest;
	}

	public void setBrokerInterest(Double brokerInterest) {
		this.brokerInterest = brokerInterest;
	}

	public Double getTotalBought() {
		return totalBought;
	}

	public void setTotalBought(Double totalBought) {
		this.totalBought = totalBought;
	}

	public Double getTotalSold() {
		return totalSold;
	}

	public void setTotalSold(Double totalSold) {
		this.totalSold = totalSold;
	}

	public Double getFinalStopLoss() {
		return finalStopLoss;
	}

	public void setFinalStopLoss(Double finalStopLoss) {
		this.finalStopLoss = finalStopLoss;
	}

	public Double getFinalTakeProfit() {
		return finalTakeProfit;
	}

	public void setFinalTakeProfit(Double finalTakeProfit) {
		this.finalTakeProfit = finalTakeProfit;
	}

	public Double getOpenCommission() {
		return openCommission;
	}

	public void setOpenCommission(Double openCommission) {
		this.openCommission = openCommission;
	}

	public Double getCloseCommission() {
		return closeCommission;
	}

	public void setCloseCommission(Double closeCommission) {
		this.closeCommission = closeCommission;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getItemBought() {
		return itemBought;
	}

	public void setItemBought(Long itemBought) {
		this.itemBought = itemBought;
	}

	public Long getItemSold() {
		return itemSold;
	}

	public void setItemSold(Long itemSold) {
		this.itemSold = itemSold;
	}

	public Double getRisk() {
		return risk;
	}

	public void setRisk(Double risk) {
		this.risk = risk;
	}

	public Long getEstimatedItems() {
		return estimatedItems;
	}

	public void setEstimatedItems(Long estimatedItems) {
		this.estimatedItems = estimatedItems;
	}

	public Long getPartsClosed() {
		return partsClosed;
	}

	public void setPartsClosed(Long partsClosed) {
		this.partsClosed = partsClosed;
	}

	@Transient
	public boolean isLong() {
		return "long".equals(position);
	}

	@Transient
	public boolean isClosed() {
		return dateClose != null;
	}
	@Transient
	public boolean isShort() {
		return "short".equals(position);
	}

	@Transient
	public double getVolume() {
		return isLong() ? totalBought : totalSold;
	}

	@Transient
	public double getFees() {
		double fees = 0;
		fees += openCommission == null ? 0 : openCommission;
		fees += closeCommission == null ? 0 : closeCommission;
		fees += brokerInterest == null ? 0 : brokerInterest;
		return fees;
	}

	@Transient
	public double getProfit() {
		if (!isClosed()) {
			return 0;
		}
		return Math.abs(totalSold - totalBought) - getFees();
	}

	@Transient
	public boolean isPartial() {
		return partsClosed > 1;
	}
}
