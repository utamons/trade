package com.corn.trade.trade;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class AutoUpdate {
	private boolean autoUpdate;

	private final Set<Consumer<Boolean>> listeners = new HashSet<>();

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void addListener(Consumer<Boolean> listener) {
		listeners.add(listener);
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		listeners.forEach(listener -> listener.accept(autoUpdate));
	}
}
