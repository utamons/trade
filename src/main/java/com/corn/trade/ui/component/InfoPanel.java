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
package com.corn.trade.ui.component;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

	private final InfoField price, spread, time, maxRangePassed, pl, maxRangeLeft, sl, be, tp, out, rr, risk;

	private InfoPanel(int fontSize, int vPadding, int hPadding, int vgap, int hgap, int hSpacing) {
		// Set the component layout
		setLayout(new GridLayout(6, 2, hgap, vgap));
		setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
		//setBackground(Color.LIGHT_GRAY);

		price = new InfoField("Price:", fontSize, 0, hSpacing, 30);
		spread = new InfoField("Spread:", fontSize, 0, hSpacing, 30);
		time = new InfoField("Time:", fontSize, 0, hSpacing, 30);
		pl = new InfoField("P/L(daily):", fontSize, 0, hSpacing, 30);
		maxRangePassed = new InfoField("MR(pass):", fontSize, 0, hSpacing, 30);
		maxRangeLeft = new InfoField("MR(left):", fontSize, 0, hSpacing, 30);
		sl = new InfoField("SL:", fontSize, 0, hSpacing, 30);
		be = new InfoField("BE:", fontSize, 0, hSpacing, 30);
		tp = new InfoField("TP:", fontSize, 0, hSpacing, 30);
		out = new InfoField("Out:", fontSize, 0, hSpacing, 30);
		rr = new InfoField("R/R:", fontSize, 0, hSpacing, 30);
		risk = new InfoField("Risk:", fontSize, 0, hSpacing, 30);

		add(time);
		add(pl);

		add(maxRangePassed);
		add(maxRangeLeft);

		add(price);
		add(spread);

		add(sl);
		add(be);

		add(tp);
		add(out);

		add(rr);
		add(risk);
	}

	public void setRisk(String risk) {
		this.risk.setInfoText(risk);
	}

	public void setRR(String rr) {
		this.rr.setInfoText(rr,true);
	}

	public void setOut(String out) {
		this.out.setInfoText(out);
	}

	public void setTp(String tp) {
		this.tp.setInfoText(tp);
	}

	public void setBe(String be) {
		this.be.setInfoText(be);
	}

	public void setSl(String sl) {
		this.sl.setInfoText(sl);
	}

	public void setPrice(String price) {
		this.price.setInfoText(price);
	}

	public void setTime(String time, Color color) {
		this.time.setInfoText(time);
		this.time.setInfoFieldColor(color);
	}

	public void setSpread(String spread) {
		this.spread.setInfoText(spread);
	}

	public void setPnl(double pnl) {
		String pl = String.format("%.2f", pnl);
		this.pl.setInfoText(pl);
		if (pnl > 0) {
			this.pl.setInfoFieldColor(Color.GREEN.darker());
		} else if (pnl < 0) {
			this.pl.setInfoFieldColor(Color.RED.darker());
		} else {
			this.pl.setInfoFieldColor(UIManager.getColor("Label.foreground"));
		}
	}

	public void setMaxRangePassed(String maxRangePassed) {
		this.maxRangePassed.setInfoText(maxRangePassed,true);
	}

	public void setMaxRangeLeft(String maxRangeLeft) {
		this.maxRangeLeft.setInfoText(maxRangeLeft, true);
	}

	public void setTime(String time) {
		this.time.setInfoText(time);
	}

	public void clear() {
		price.clear();
		spread.clear();
		time.clear();
		maxRangePassed.clear();
		maxRangeLeft.clear();
		sl.clear();
		be.clear();
		tp.clear();
		out.clear();
		rr.clear();
		risk.clear();
	}

	public static final class InfoPanelBuilder {
		private int fontSize;
		private int vPadding;
		private int hPadding;
		private int vgap;
		private int hgap;
		private int hSpacing;

		private InfoPanelBuilder() {
		}

		public static InfoPanelBuilder anInfoPanel() {
			return new InfoPanelBuilder();
		}

		public InfoPanelBuilder withFontSize(int fontSize) {
			this.fontSize = fontSize;
			return this;
		}

		public InfoPanelBuilder withVPadding(int vPadding) {
			this.vPadding = vPadding;
			return this;
		}

		public InfoPanelBuilder withHPadding(int hPadding) {
			this.hPadding = hPadding;
			return this;
		}

		public InfoPanelBuilder withVgap(int vgap) {
			this.vgap = vgap;
			return this;
		}

		public InfoPanelBuilder withHgap(int hgap) {
			this.hgap = hgap;
			return this;
		}

		public InfoPanelBuilder withHSpacing(int hSpacing) {
			this.hSpacing = hSpacing;
			return this;
		}

		public InfoPanel build() {
			return new InfoPanel(fontSize, vPadding, hPadding, vgap, hgap, hSpacing);
		}
	}
}
