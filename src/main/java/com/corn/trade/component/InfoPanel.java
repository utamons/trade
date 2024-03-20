package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

	private final InfoField price, spread, time, adrPassed, pl, adrLeft, sl, be, tp, out, rr, risk;

	private InfoPanel(int fontSize, int vPadding, int hPadding, int vgap, int hgap, int hSpacing) {
		// Set the component layout
		setLayout(new GridLayout(4, 3, hgap, vgap));
		setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
		//setBackground(Color.LIGHT_GRAY);

		price = new InfoField("Price:", fontSize, 0, hSpacing, 30);
		spread = new InfoField("Spread:", fontSize, 0, hSpacing, 30);
		time = new InfoField("Time:", fontSize, 0, hSpacing, 30);
		pl = new InfoField("P/L:", fontSize, 0, hSpacing, 30);
		adrPassed = new InfoField("ADR (p):", fontSize, 0, hSpacing, 30);
		adrLeft = new InfoField("ADR (l):", fontSize, 0, hSpacing, 30);
		sl = new InfoField("SL:", fontSize, 0, hSpacing, 30);
		be = new InfoField("BE:", fontSize, 0, hSpacing, 30);
		tp = new InfoField("TP:", fontSize, 0, hSpacing, 30);
		out = new InfoField("Out:", fontSize, 0, hSpacing, 30);
		rr = new InfoField("R/R:", fontSize, 0, hSpacing, 30);
		risk = new InfoField("Risk:", fontSize, 0, hSpacing, 30);

		add(price);
		add(spread);
		add(time);

		add(pl);
		add(adrPassed);
		add(adrLeft);

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
		this.rr.setInfoText(rr);
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

	public void setPl(String pl) {
		this.pl.setInfoText(pl);
	}

	public void setAdrPassed(String adrPassed) {
		this.adrPassed.setInfoText(adrPassed);
	}

	public void setAdrLeft(String adrLeft) {
		this.adrLeft.setInfoText(adrLeft);
	}

	public void setTime(String time) {
		this.time.setInfoText(time);
	}

	public void clear() {
		price.clear();
		spread.clear();
		time.clear();
		pl.clear();
		adrPassed.clear();
		adrLeft.clear();
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
