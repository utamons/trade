package com.corn.trade.common;

import java.util.ArrayList;
import java.util.List;

public abstract class Notifier {
	private final List<Runnable> triggers = new ArrayList<>();

	public void addUpdater(Runnable trigger) {
		triggers.add(trigger);
	}

	public void announce() {
		triggers.forEach(Runnable::run);
	}
}
