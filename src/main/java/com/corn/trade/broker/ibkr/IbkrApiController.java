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

import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController;

// Should not be unit tested
public class IbkrApiController extends ApiController {
	public IbkrApiController(IConnectionHandler handler, ApiConnection.ILogger inLogger, ApiConnection.ILogger outLogger) {
		super(handler, inLogger, outLogger);
	}

	public boolean isConnected() {
		return client().isConnected() && checkConnection();
	}
}
