package com.corn.trade.ui.view;

import com.corn.trade.ui.component.position.PositionRow;

public interface PositionView {
	PositionRow addPosition(String label);

	void removePosition(String label);
}
