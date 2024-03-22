package com.corn.trade.ui.view;

import com.corn.trade.ui.component.*;

import java.awt.*;

public interface TradeView {
	LabeledDoubleField techSL();
	LabeledDoubleField goal();
	LabeledDoubleField level();
	LabeledLookup assetLookup();
	InfoPanel info();
	LabeledComboBox exchangeBox();
	MessagePanel messagePanel();
	TrafficLight trafficLight();
	Component asComponent();
}
