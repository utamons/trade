/*
	Trade
    Copyright (C) 2024  Cornknight

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.corn.trade.ui.controller;

import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
import com.corn.trade.ui.view.PositionPanel;
import com.corn.trade.ui.view.TradeView;

public interface TradeViewListener {
	void onExchangeChange(String exchangeName);
	void onAssetChange(String assetName);
	void onPositionTypeChange(PositionType positionType);
	void onEstimationTypeChange(EstimationType estimationType);
	void onLevelChange(Double level);
	void onTechStopLossChange(Double techStopLoss);
	void onGoalChange(Double goal);
	void setView(TradeView tradeView);
	void setPositionPanel(PositionPanel positionPanel);
	void onLock();
	void onLimit();
	void onStopLimit();
	void checkRisk();
}
