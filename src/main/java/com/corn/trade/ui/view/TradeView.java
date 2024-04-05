package com.corn.trade.ui.view;

import com.corn.trade.ui.component.*;

import javax.swing.*;
import java.awt.*;

public interface TradeView {
	LabeledDoubleField techSL();
	LabeledDoubleField goal();
	LabeledDoubleField level();
	LabeledLookup assetLookup();
	InfoPanel info();
	LabeledComboBox exchangeBox();
	LabeledComboBox positionTypeBox();
	LabeledComboBox estimationBox();
	MessagePanel messagePanel();
	TrafficLight trafficLight();
	JButton lockButton();
	JButton stopLimitButton();
	JButton limitButton();
	PositionPanel getPositionPanel();
	Component asComponent();
}
