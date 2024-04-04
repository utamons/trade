package com.corn.trade.broker.ibkr;

import com.corn.trade.broker.iPositionSubscriber;
import com.corn.trade.model.Position;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.controller.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is a subscriber for IBKR positions.
 * It supports a list of positions, and subscribes to positions updates and to P/L updates for each position.
 * If there are no positions, it will cancel the subscription.
 * It also supports a list of listeners, which will be notified when a position is updated.
 */
public class IbkrPositionSubscriber implements iPositionSubscriber {
	private final static Logger log = LoggerFactory.getLogger(IbkrPositionSubscriber.class);

	private final Map<String, Position>           positions = new HashMap<>();
	private final Map<String, Consumer<Position>> listeners = new HashMap<>();
	private final IbkrConnectionHandler          connectionHandler;
	private final ApiController.IAccountHandler accountHandler;

	/*
	  todo:
	   Не надо забывать, что при запросе передаются данные по всем позициям текущего дня,
	   в том числе и по закрытым. Более того, если все позиции закрыты, всё равно данные обновляются
	   раз в три минуты.
	   Поэтому отправлять данные листенерам нужно только по открытым позициям, и только если обновлена
	   именно позиция листенера.
	   realizedPnl - кумулятивная величина, и накапливается по позиции за день
	 */
	IbkrPositionSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;

		accountHandler = new ApiController.IAccountHandler() {
			@Override
			public void accountValue(String account, String key, String value, String currency) {
				//log.info("accountValue: {} {} {} {}", account, key, value, currency);
			}

			@Override
			public void accountTime(String timeStamp) {
				//log.info("accountTime: {}", timeStamp);
			}

			@Override
			public void accountDownloadEnd(String account) {
				log.info("accountDownloadEnd: {}", account);
			}

			@Override
			public void updatePortfolio(com.ib.controller.Position iPos) {
				log.info("updatePortfolio: positions {}", positions.size());
				log.info("updatePortfolio: symbol {}", iPos.contract().symbol());

				Contract contract = iPos.contract();
				Decimal pos = iPos.position();
				Double avgCost = iPos.averageCost();
				Double unrealizedPnl = iPos.unrealPnl();
				Double realizedPnl = iPos.realPnl();
				Position position = positions.get(contract.symbol());
				if (position == null && pos.longValue() > 0) {
					log.info("updatePortfolio: new position created");
					position = Position.aPosition()
					                   .withSymbol(contract.symbol())
					                   .withQuantity(pos.longValue())
					                   .withAveragePrice(avgCost)
					                   .withMarketValue(iPos.marketValue())
					                   .withUnrealizedPnl(unrealizedPnl)
					                   .withRealizedPnl(realizedPnl)
					                   .build();
					positions.put(contract.symbol(), position);
				} else if (position != null) {
					log.info("updatePortfolio: position updated");
					position = position.copy()
					                   .withQuantity(pos.longValue())
					                   .withAveragePrice(avgCost)
					                   .withMarketValue(iPos.marketValue())
					                   .withUnrealizedPnl(unrealizedPnl)
					                   .withRealizedPnl(realizedPnl)
					                   .build();
					positions.put(contract.symbol(), position);
				}
				notifyListeners();
			}
		};
	}

	private void notifyListeners() {
		log.info("notifyListeners: listeners {}", listeners.size());
		positions.forEach((assetName, position) -> {
			if (listeners.containsKey(assetName)) {
				log.info("notifyListeners: listener found for {}", assetName);
				listeners.get(assetName).accept(position);
			}
		});
	}

	@Override
	public void addListener(String assetName, Consumer<Position> listener) {
		listeners.put(assetName, listener);
	}

	/**
	 * Once new position is awaited, we should request for it, but we must be sure that the main order is already executed.
	 * Otherwise, we might not get the new position for future updates.
	 */
	@Override
	public void request() {
		connectionHandler.controller().reqAccountUpdates(true, "", accountHandler);
	}
}
