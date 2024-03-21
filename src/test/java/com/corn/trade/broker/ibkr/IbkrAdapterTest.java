package com.corn.trade.broker.ibkr;

import static org.mockito.Mockito.*;

import com.corn.trade.util.Trigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ib.controller.ApiController;

class IbkrAdapterTest {

	private IbkrAdapter adapter;
	private ApiController controller;
	private Trigger       disconnectionListener;

	@BeforeEach
	void setUp() {
		controller = mock(ApiController.class);
		adapter = spy(new IbkrAdapter());
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

