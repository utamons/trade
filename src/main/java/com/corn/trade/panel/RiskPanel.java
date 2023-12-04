package com.corn.trade.panel;

import com.corn.trade.component.LabeledDoubleField;
import com.corn.trade.trade.AutoUpdate;
import com.corn.trade.trade.Calculator;

import java.awt.*;

public class RiskPanel extends BasePanel {

	public RiskPanel(Calculator calculator, AutoUpdate autoUpdate, Dimension maxSize, Dimension minSize, int spacing, int fieldHeight) {
		super("Risk", calculator, autoUpdate, maxSize, minSize);

		LabeledDoubleField risk = new LabeledDoubleField("Risk:", 10, null, spacing, fieldHeight, false, null);

		LabeledDoubleField riskPc = new LabeledDoubleField("Risk (%):", 10, null, spacing, fieldHeight, false, null);

		LabeledDoubleField riskReward = new LabeledDoubleField("R/R:", 10, null, spacing, fieldHeight,  false,null);

		this.add(riskReward);
		this.add(risk);
		this.add(riskPc);

		calculator.addTrigger(() -> {
			risk.setValue(calculator.getRisk());
			riskPc.setValue(calculator.getRiskPercent());
			riskReward.setValue(calculator.getRiskRewardRatioPercent());
		});
	}
}
