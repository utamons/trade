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
package com.corn.trade.broker.ibkr;

public class IbkrSubscriberFactory {
	private static       IbkrPositionSubscriber positionSubscriber;
	private static       IbkrPnLSubscriber      pnlSubscriber;


	static synchronized IbkrPositionSubscriber getPositionSubscriber() {
		if (positionSubscriber == null) {
			IbkrConnectionHandler connectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			positionSubscriber = new IbkrPositionSubscriber(connectionHandler);
		}
		return positionSubscriber;
	}

	static synchronized IbkrPnLSubscriber getPnlSubscriber() {
		if (pnlSubscriber == null) {
			IbkrConnectionHandler connectionHandler = IbkrConnectionHandlerFactory.getConnectionHandler();
			pnlSubscriber = new IbkrPnLSubscriber(connectionHandler);
		}
		return pnlSubscriber;
	}
}
