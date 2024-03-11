package com.corn.trade.component;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

	private final InfoField price, zone, time, adrPassed, pl, adrLeft, sl, be, tp, out, rr, risk;

	public InfoPanel(int fontSize, int vPadding, int hPadding, int vgap, int hgap, int hSpacing) {
		// Set the component layout
		setLayout(new GridLayout(4, 3, hgap, vgap));
		setBorder(BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding));
		//setBackground(Color.LIGHT_GRAY);

		price = new InfoField("Ask:", fontSize, 0, hSpacing, 30);
		zone = new InfoField("Zone:", fontSize, 0, hSpacing, 30);
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
		add(zone);
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
}
