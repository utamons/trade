package com.corn.trade.ui.view;

import com.corn.trade.service.AssetService;
import com.corn.trade.type.EstimationType;
import com.corn.trade.type.PositionType;
import com.corn.trade.ui.component.*;
import com.corn.trade.ui.controller.TradeViewListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TradePanel extends BasePanel implements TradeView {

	public static final int                TEXT_FIELD_SIZE = 10;
	private final       LabeledComboBox    exchangeBox;
	private final       MessagePanel       messagePanel;
	private final       LabeledLookup      assetLookup;
	private final       InfoPanel          info;
	private final       LabeledDoubleField goal;
	private final       LabeledDoubleField level;
	private final       LabeledDoubleField techSL;
	private final       TrafficLight       trafficLight;
	private final       LabeledComboBox    positionBox;
	private final       LabeledComboBox    estimationBox;
	private final       JButton            lockBtn;
	private final       JButton            stopLimitBtn;
	private final       JButton            limitBtn;
	private final PositionPanel positionPanel;

	public TradePanel(TradeViewListener controller, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super(maxSize, minSize);

		controller.setView(this);

		AssetService assetService = new AssetService();

		this.setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		java.util.List<String> assets = assetService.getAssetNames();

		assetLookup = new LabeledLookup("Asset:", assets, spacing, fieldHeight, controller::onAssetChange);

		final List<String> exchanges = assetService.getExchangeNames();


		exchangeBox = new LabeledComboBox("Exchange:", exchanges, spacing, fieldHeight, controller::onExchangeChange);

		positionBox = new LabeledComboBox("Position:",
		                                  PositionType.getValues(),
		                                  spacing,
		                                  fieldHeight,
		                                  (positionType) -> controller.onPositionTypeChange(PositionType.fromString(positionType)));

		estimationBox = new LabeledComboBox("Estimation:",
		                                    EstimationType.getValues(),
		                                    spacing,
		                                    fieldHeight,
		                                    (estimationType) -> controller.onEstimationTypeChange(EstimationType.fromString(
				                                    estimationType)));

		level = new LabeledDoubleField("Level:", TEXT_FIELD_SIZE, spacing, fieldHeight, false, controller::onLevelChange);

		goal = new LabeledDoubleField("Goal:", TEXT_FIELD_SIZE, spacing, fieldHeight, false, controller::onGoalChange);
		goal.setEditable(false);

		techSL = new LabeledDoubleField("Tech. SL:",
		                                TEXT_FIELD_SIZE,
		                                spacing,
		                                fieldHeight,
		                                true,
		                                controller::onTechStopLossChange);

		trafficLight = new TrafficLight();

		RowPanel orderPanel = new RowPanel(20);

		lockBtn = new JButton("Lock");
		lockBtn.addActionListener(e -> controller.onLock());

		stopLimitBtn = new JButton("Stop-Limit");
		stopLimitBtn.addActionListener(e -> controller.onStopLimit());
		stopLimitBtn.setEnabled(false);

		limitBtn = new JButton("Limit");
		limitBtn.addActionListener(e -> controller.onLimit());
		limitBtn.setEnabled(false);

		orderPanel.add(lockBtn);
		orderPanel.add(trafficLight);
		orderPanel.add(stopLimitBtn);
		orderPanel.add(limitBtn);

		positionPanel = new PositionPanel();
		info = InfoPanel.InfoPanelBuilder.anInfoPanel()
		                                 .withFontSize(15)
		                                 .withVPadding(20)
		                                 .withHPadding(5)
		                                 .withVgap(20)
		                                 .withHgap(42)
		                                 .withHSpacing(5)
		                                 .build();

		messagePanel = new MessagePanel(20, 0);
		messagePanel.info("Welcome to Trade!");

		panel.add(exchangeBox);
		panel.add(assetLookup);
		panel.add(positionBox);
		panel.add(estimationBox);
		panel.add(level);
		panel.add(goal);
		panel.add(techSL);
		panel.add(messagePanel);
		panel.add(orderPanel);
		panel.add(positionPanel);
		panel.add(info);

		this.add(panel, BorderLayout.NORTH);

		controller.onExchangeChange(exchanges.get(0));
		controller.onPositionTypeChange(PositionType.LONG);
		controller.onEstimationTypeChange(EstimationType.fromString(EstimationType.getValues().get(0)));
		controller.setPositionPanel(positionPanel);
	}

	@Override
	public LabeledComboBox positionTypeBox() {
		return positionBox;
	}

	@Override
	public LabeledComboBox estimationBox() {
		return estimationBox;
	}

	@Override
	public LabeledDoubleField techSL() {
		return techSL;
	}

	@Override
	public LabeledDoubleField level() {
		return level;
	}

	@Override
	public LabeledDoubleField goal() {
		return goal;
	}

	@Override
	public LabeledLookup assetLookup() {
		return assetLookup;
	}

	@Override
	public InfoPanel info() {
		return info;
	}

	@Override
	public LabeledComboBox exchangeBox() {
		return exchangeBox;
	}

	@Override
	public MessagePanel messagePanel() {
		return messagePanel;
	}

	@Override
	public TrafficLight trafficLight() {
		return trafficLight;
	}

	@Override
	public JButton lockButton() {
		return lockBtn;
	}

	@Override
	public JButton stopLimitButton() {
		return stopLimitBtn;
	}

	@Override
	public JButton limitButton() {
		return limitBtn;
	}

	@Override
	public PositionPanel getPositionPanel() {
		return positionPanel;
	}

	@Override
	public Component asComponent() {
		return this;
	}
}
