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

import static org.mockito.Mockito.*;

import com.corn.trade.util.Trigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ib.controller.ApiController;

class IbkrConnectionHandlerTest {

	private IbkrConnectionHandler adapter;
	private ApiController         controller;
	private Trigger               disconnectionListener;

	@BeforeEach
	void setUp() {
		controller = mock(ApiController.class);
		adapter = spy(new IbkrConnectionHandler());
		doReturn(controller).when(adapter).controller();
		disconnectionListener = mock(Trigger.class);
		adapter.setDisconnectionListener(disconnectionListener);
	}

	@Test
	void testRun() {
		String defaultHost = "127.0.0.1";
		int defaultPort = 7496;

		adapter.run();
		verify(controller).connect(eq(defaultHost), eq(defaultPort), eq(0), isNull());
	}

	@Test
	void testDisconnected() {
		adapter.disconnected();
		verify(disconnectionListener, times(1)).trigger();
	}
}

