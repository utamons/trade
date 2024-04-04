package com.corn.trade.broker;

import com.corn.trade.model.Position;

import java.util.function.Consumer;

public interface iPositionSubscriber {
	void addListener(String assetName, Consumer<Position> listener);

	void request();
}
