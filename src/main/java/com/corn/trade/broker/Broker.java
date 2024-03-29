package com.corn.trade.broker;

import com.corn.trade.entity.Order;
import com.corn.trade.entity.Trade;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.JpaUtil;
import com.corn.trade.model.Bar;
import com.corn.trade.model.TradeContext;
import com.corn.trade.model.TradeData;
import com.corn.trade.service.OrderService;
import com.corn.trade.service.TradeService;
import com.corn.trade.type.*;
import com.corn.trade.util.ChangeOrderListener;
import com.corn.trade.util.Util;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.corn.trade.util.Util.round;

public abstract class Broker {
	public static final    int                                      BARS_FOR_ADR      = 10;
	protected static final int                                      ADR_BARS          = 20;
	private static final   Logger                                   log               =
			LoggerFactory.getLogger(Broker.class);
	protected final        HashMap<Integer, Consumer<TradeContext>> contextListeners  = new HashMap<>();
	protected              List<Bar>                                adrBarList        = new java.util.ArrayList<>();
	protected              String                                   exchangeName;
	protected              Double                                   adr;
	protected              Double                                   ask;
	protected              Double                                   bid;
	protected              Double                                   price;
	protected              Double                                   dayHigh;
	protected              Double                                   dayLow;
	protected              int                                      contextListenerId = 0;
	private                String                                   assetName;
	private                String                                   name;

	protected void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public synchronized int requestTradeContext(Consumer<TradeContext> tradeContextListener) throws BrokerException {
		if (contextListeners.isEmpty()) {
			requestAdr();
			requestMarketData();
		}
		contextListeners.put(++contextListenerId, tradeContextListener);
		return contextListenerId;
	}

	protected abstract void requestAdr() throws BrokerException;

	protected abstract void requestMarketData() throws BrokerException;

	protected abstract void cancelMarketData();

	public void openPosition(TradeData tradeData) throws BrokerException {
		OrderService orderService = new OrderService();
		TradeService tradeService = new TradeService();

		EntityManager em = null;
		try {
			em = JpaUtil.getEntityManager();
			orderService.withEntityManager(em);
			tradeService.withEntityManager(em);

			em.getTransaction().begin();
			OrderType orderType = tradeData.getOrderStop() == null ? OrderType.LMT : OrderType.STP_LMT;

			// Create trade
			final Trade trade = tradeService.createTrade(assetName, exchangeName, tradeData);
			// Create 3 orders
			final Order mainOrder = orderService.createOrder(trade, tradeData, OrderRole.MAIN, orderType, null);
			final Order takeProfitOrder = orderService.createOrder(trade, tradeData, OrderRole.TAKE_PROFIT, OrderType.LMT, mainOrder);
			final Order stopLossOrder = orderService.createOrder(trade, tradeData, OrderRole.STOP_LOSS, OrderType.STP, mainOrder);
			// Create listeners to update orders and trade status
			ChangeOrderListener mainOrderListener = getChangeOrderListener(mainOrder.getId(), trade.getId(), OrderRole.MAIN);
			ChangeOrderListener tpOrderListener = getChangeOrderListener(takeProfitOrder.getId(), trade.getId(), OrderRole.TAKE_PROFIT);
			ChangeOrderListener slOrderListener = getChangeOrderListener(stopLossOrder.getId(), trade.getId(), OrderRole.STOP_LOSS);

			// Place orders
			OrderBracketIds bracketIds = placeOrderWithBracket(tradeData.getQuantity(),
			                                                   tradeData.getOrderStop(),
			                                                   tradeData.getOrderLimit(),
			                                                   tradeData.getTechStopLoss() ==
			                                                   null ? tradeData.getStopLoss() : tradeData.getTechStopLoss(),
			                                                   tradeData.getTakeProfit(),
			                                                   tradeData.getPositionType(),
			                                                   orderType,
			                                                   mainOrderListener,
			                                                   tpOrderListener,
			                                                   slOrderListener);

			// Update order ids
			orderService.updateOrderId(mainOrder.getId(), bracketIds.mainId());
			orderService.updateOrderId(takeProfitOrder.getId(), bracketIds.takeProfitId());
			orderService.updateOrderId(stopLossOrder.getId(), bracketIds.stopLossId());

			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("openPosition error: ", e);
			if (em != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			throw new BrokerException(e.getMessage(), e);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	private static ChangeOrderListener getChangeOrderListener(Long id, Long tradeId, OrderRole role) {
		return new ChangeOrderListener() {
			@Override
			public void onOrderChange(long orderId, OrderStatus status, long filled, long remaining, double avgFillPrice) {
				OrderService orderService = new OrderService();
				TradeService tradeService = new TradeService();

				EntityManager em = null;
				try {
					em = JpaUtil.getEntityManager();
					orderService.withEntityManager(em);
					tradeService.withEntityManager(em);

					em.getTransaction().begin();

					orderService.updateOrder(id, orderId, status, filled, remaining, avgFillPrice);
					if (role == OrderRole.MAIN && status == OrderStatus.FILLED) {
						tradeService.updateTradeStatus(tradeId, TradeStatus.OPEN);
					}
					if ((role == OrderRole.TAKE_PROFIT || role == OrderRole.STOP_LOSS) && status == OrderStatus.FILLED) {
						tradeService.updateTradeStatus(tradeId, TradeStatus.CLOSED);
					}
					em.getTransaction().commit();
				} catch (Exception e) {
					log.error("ChangeOrderListener error: ", e);
					if (em != null && em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
					throw new RuntimeException(e);
				} finally {
					if (em != null && em.isOpen()) {
						em.close();
					}
				}
			}

			@Override
			public void onOrderError(long id, String errorCode, String errorMsg) {
				OrderService orderService = new OrderService();
				try {
					orderService.updateOrderError(id, errorCode, errorMsg);
				} catch (DBException e) {
					log.error("ChangeOrderListener error: ", e);
					throw new RuntimeException(e);
				}
			}
		};
	}

	public abstract OrderBracketIds placeOrderWithBracket(long qtt,
	                                                      Double stop,
	                                                      Double limit,
	                                                      Double stopLoss,
	                                                      Double takeProfit,
	                                                      PositionType positionType,
	                                                      OrderType orderType,
	                                                      ChangeOrderListener mainOrderListener,
	                                                      ChangeOrderListener tpOrderListener,
	                                                      ChangeOrderListener slOrderListener
	                                                      ) throws BrokerException;

	protected void notifyTradeContext() throws BrokerException {
		calculateFilteredAdr();
		if (contextListeners.isEmpty()) return;
		TradeContext context = createTradeContext();
		contextListeners.values().forEach(listener -> listener.accept(context));
	}

	private void calculateFilteredAdr() throws BrokerException {
		if (adr != null) return;
		if (adrBarList.isEmpty()) {
			throw new BrokerException("No ADR data available.");
		}

		final double ADR_TOLERANCE_PERCENTAGE = 40.0;

		adrBarList.sort(Comparator.comparingLong(Bar::getTime));

		List<Double> rangeList = adrBarList.stream().map((b) -> (b.getHigh() - b.getLow())).toList();

		double initialAverage = rangeList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

		double toleranceValue = initialAverage * (ADR_TOLERANCE_PERCENTAGE / 100);

		List<Double> filteredList =
				rangeList.stream().filter(value -> Math.abs(value - initialAverage) <= toleranceValue).toList();

		if (filteredList.isEmpty()) {
			adr = round(initialAverage);
		}

		adr = round(getLastSome(filteredList).stream().mapToDouble(Double::doubleValue).average().orElse(initialAverage));

		log.debug("Initial ADR: {}", round(initialAverage));
		log.debug("ADR: {}", adr);
		log.debug("ADR Range: {}",
		          String.join(",", getLastSome(filteredList).stream().map(Util::round).map(Object::toString).toList()));
	}

	protected TradeContext createTradeContext() {
		return TradeContext.TradeContextBuilder.aTradeContext()
		                                       .withAsk(ask)
		                                       .withBid(bid)
		                                       .withPrice(price)
		                                       .withDayHigh(dayHigh)
		                                       .withDayLow(dayLow)
		                                       .withAdr(adr)
		                                       .build();
	}

	public void cancelTradeContext(int id) {
		this.contextListeners.remove(id);
		if (this.contextListeners.isEmpty()) {
			cancelMarketData();
		}
	}

	public List<Double> getLastSome(List<Double> rangeList) {
		if (rangeList.size() > BARS_FOR_ADR) {
			return rangeList.subList(rangeList.size() - BARS_FOR_ADR, rangeList.size());
		} else {
			return rangeList;
		}
	}
}
