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
}
