package com.corn.trade.broker.ibkr;

import com.corn.trade.model.Position;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.controller.ApiController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is a subscriber for IBKR positions.
 * It supports a list of positions, and subscribes to positions updates and to P/L updates for each position.
 * If there are no positions, it will cancel the subscription.
 * It also supports a list of listeners, which will be notified when a position is updated.
 */
public class IbkrPositionSubscriber {
	private final Map<String, Position>           positions = new HashMap<>();
	private final Map<String, Consumer<Position>> listeners = new HashMap<>();
	private final IbkrConnectionHandler           connectionHandler;
	private final ApiController.IPositionHandler  positionHandler;

	public IbkrPositionSubscriber(IbkrConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;

		positionHandler = new ApiController.IPositionHandler() {
			@Override
			public void position(String account, Contract contract, Decimal pos, double avgCost) {
				Position position = positions.get(contract.symbol());
				if (position == null) {
					position = Position.aPosition()
					                    .withSymbol(contract.symbol())
					                    .withQuantity(pos.longValue())
					                    .withAveragePrice(avgCost)
					                    .build();
				} else {
					position = position.toBuilder()
					                   .withQuantity(pos.longValue())
					                   .withAveragePrice(avgCost)
					                   .build();
				}
				positions.put(contract.symbol(), position);
			}

			@Override
			public void positionEnd() {
				if (positions.isEmpty()) {
					unsubscribe();
				}
			}
		};
	}


	public void addListener(String assetName, Consumer<Position> listener) {
		if (listeners.containsKey(assetName)) {
			throw new IllegalArgumentException("Listener for asset " + assetName + " already exists");
		}
		listeners.put(assetName, listener);
	}

	public void removeListener(String assetName) {
		listeners.remove(assetName);
	}

	/**
	 * Once new position is awaited, we should request for it, but we must be sure that the main order is already executed.
	 * Otherwise, we might not get the new position for future updates.
	 */
	public void request() {
		connectionHandler.controller().reqAccountUpdates(true, "", positionHandler);
	}

	public void unsubscribe() {
		connectionHandler.unsubscribeFromPositions();
	}

}
