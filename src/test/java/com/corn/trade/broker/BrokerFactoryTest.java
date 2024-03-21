package com.corn.trade.broker;

import com.corn.trade.broker.ibkr.IbkrAdapter;
import com.corn.trade.broker.ibkr.IbkrAdapterFactory;
import com.corn.trade.broker.ibkr.IbkrBroker;
import com.corn.trade.util.Trigger;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerFactoryTest {

	private Trigger     disconnectionTrigger;
	private IbkrAdapter mockedIbkrAdapter;
	private Contract    mockedContract;

	@BeforeEach
	void setUp() {
		BrokerFactory.clearCache();
		disconnectionTrigger = mock(Trigger.class);
		ContractDetails mockedContractDetails = mock(ContractDetails.class);
		mockedIbkrAdapter = mock(IbkrAdapter.class);
		mockedContract = mock(Contract.class);
		when(mockedIbkrAdapter.isConnected()).thenReturn(true);
		when(mockedIbkrAdapter.lookupContract(any(Contract.class))).thenReturn(List.of(mockedContractDetails));
		when(mockedContract.currency()).thenReturn("USD");
		when(mockedContractDetails.contract()).thenReturn(mockedContract);
	}

	@Test
	void testCreationANewIbkrBroker() throws BrokerException {
		String brokerName   = "IBKR";
		String assetName    = "AAPL";
		String exchangeName = "NASDAQ";

		when(mockedContract.primaryExch()).thenReturn(exchangeName);
		when(mockedContract.symbol()).thenReturn(assetName);

		try (MockedStatic<IbkrAdapterFactory> mockedFactory = Mockito.mockStatic(IbkrAdapterFactory.class)) {
			mockedFactory.when(IbkrAdapterFactory::getAdapter).thenReturn(mockedIbkrAdapter);

			// Call the method under test
			Broker broker = BrokerFactory.getBroker(brokerName, assetName, exchangeName, disconnectionTrigger);

			// Assertions and verifications
			assertNotNull(broker);
			assertInstanceOf(IbkrBroker.class, broker);
			assertEquals(exchangeName, broker.getExchangeName());
		}
	}

	@Test
	void testGetCachedIbkrBroker() throws BrokerException {
		String brokerName   = "IBKR";
		String assetName    = "AAPL";
		String exchangeName = "NASDAQ";


		when(mockedContract.primaryExch()).thenReturn(exchangeName);
		when(mockedContract.symbol()).thenReturn(assetName);

		try (MockedStatic<IbkrAdapterFactory> mockedFactory = Mockito.mockStatic(IbkrAdapterFactory.class)) {
			mockedFactory.when(IbkrAdapterFactory::getAdapter).thenReturn(mockedIbkrAdapter);

			// First call to getBroker to populate the cache
			Broker initialBroker = BrokerFactory.getBroker(brokerName, assetName, exchangeName, disconnectionTrigger);

			// Second call to getBroker for the same parameters should return the cached broker
			Broker cachedBroker = BrokerFactory.getBroker(brokerName, assetName, exchangeName, disconnectionTrigger);

			// Verify the same broker instance is returned
			assertSame(initialBroker, cachedBroker, "Expected the cached broker instance to be returned.");
		}
	}

	@Test
	void testGetIbkrBrokerWithDifferentActualExchangeName() throws BrokerException {
		String brokerName          = "IBKR";
		String assetName           = "AAPL";
		String initialExchangeName = "NASDAQ";
		String actualExchangeName  = "NYSE";

		when(mockedContract.primaryExch()).thenReturn(actualExchangeName);
		when(mockedContract.symbol()).thenReturn(assetName);

		try (MockedStatic<IbkrAdapterFactory> mockedFactory = Mockito.mockStatic(IbkrAdapterFactory.class)) {
			mockedFactory.when(IbkrAdapterFactory::getAdapter).thenReturn(mockedIbkrAdapter);

			// Call the method under test with initialExchangeName, expecting it to use actualExchangeName
			Broker broker = BrokerFactory.getBroker(brokerName, assetName, initialExchangeName, disconnectionTrigger);

			// Verify the broker is created with the actual exchange name
			assertNotNull(broker, "Broker should not be null.");
			assertInstanceOf(IbkrBroker.class, broker, "Broker should be an instance of IbkrBroker.");
			assertEquals(actualExchangeName, broker.getExchangeName(), "Broker should have the actual exchange name.");
		}
	}
}
