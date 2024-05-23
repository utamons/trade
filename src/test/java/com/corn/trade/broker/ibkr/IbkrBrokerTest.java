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

import com.corn.trade.broker.BrokerException;
import com.corn.trade.util.Trigger;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IbkrBrokerTest {

	@BeforeEach
	void setUp() {
		// Optional: Any setup before each test
	}

	@Test
	void testInitContractSuccessfullyForNasdaq() {
		String  ticker                = "AAPL";
		String  exchangeName          = "NASDAQ";
		Trigger disconnectionListener = mock(Trigger.class);

		ContractDetails contractDetails = mock(ContractDetails.class);
		Contract expectedContract = new Contract();
		expectedContract.symbol(ticker);
		expectedContract.secType("STK");
		expectedContract.primaryExch(exchangeName);
		expectedContract.exchange("SMART");
		expectedContract.currency("USD");

		when(contractDetails.contract()).thenReturn(expectedContract);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(List.of(contractDetails));

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory =
				     Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			IbkrBroker broker = new IbkrBroker(ticker, exchangeName, disconnectionListener);

			verify(ibkrConnectionHandler, times(1)).lookupContract(eq(expectedContract));
			assertEquals(expectedContract, broker.getContractDetails().contract());
			assertEquals(exchangeName, broker.getExchangeName());
		}
	}

	@Test
	void testInitContractSuccessfullyForNyse() {
		String  ticker                = "AAPL";
		String  exchangeName          = "NYSE";
		Trigger disconnectionListener = mock(Trigger.class);

		ContractDetails contractDetails = mock(ContractDetails.class);
		Contract expectedContract = new Contract();
		expectedContract.symbol(ticker);
		expectedContract.secType("STK");
		expectedContract.primaryExch(exchangeName);
		expectedContract.exchange("SMART");
		expectedContract.currency("USD");

		when(contractDetails.contract()).thenReturn(expectedContract);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(List.of(contractDetails));

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory =
				     Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			IbkrBroker broker = new IbkrBroker(ticker, exchangeName, disconnectionListener);

			verify(ibkrConnectionHandler, times(1)).lookupContract(eq(expectedContract));
			assertEquals(expectedContract, broker.getContractDetails().contract());
			assertEquals(exchangeName, broker.getExchangeName());
		}
	}

	@Test
	void testInitContractSuccessfullyForOtherExch() {
		String  ticker                = "AAPL";
		String  exchangeName          = "OTHER_EXCHANGE";
		Trigger disconnectionListener = mock(Trigger.class);

		ContractDetails contractDetails = mock(ContractDetails.class);
		Contract expectedContract = new Contract();
		expectedContract.symbol(ticker);
		expectedContract.secType("STK");
		expectedContract.primaryExch(exchangeName);
		expectedContract.exchange(exchangeName);

		when(contractDetails.contract()).thenReturn(expectedContract);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(List.of(contractDetails));

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory =
				     Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			IbkrBroker broker = new IbkrBroker(ticker, exchangeName, disconnectionListener);

			verify(ibkrConnectionHandler, times(1)).lookupContract(eq(expectedContract));
			assertEquals(expectedContract, broker.getContractDetails().contract());
			assertEquals(exchangeName, broker.getExchangeName());
		}
	}

	@Test
	void testInitContractSuccessfullyForChangedExchange() {
		String  ticker                = "AAPL";
		String  exchangeName          = "NASDAQ";
		Trigger disconnectionListener = mock(Trigger.class);

		ContractDetails contractDetails = mock(ContractDetails.class);
		Contract contract = new Contract();
		contract.symbol(ticker);
		contract.secType("STK");
		contract.primaryExch(exchangeName);
		contract.exchange("SMART");
		contract.currency("USD");

		Contract returnedContract = new Contract();
		returnedContract.symbol(ticker);
		returnedContract.secType("STK");
		returnedContract.primaryExch("NYSE");
		returnedContract.exchange("SMART");
		returnedContract.currency("USD");

		when(contractDetails.contract()).thenReturn(returnedContract);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(List.of(contractDetails));

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory =
				     Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			IbkrBroker broker = new IbkrBroker(ticker, exchangeName, disconnectionListener);

			verify(ibkrConnectionHandler, times(1)).lookupContract(eq(contract));
			assertNotEquals(contract, broker.getContractDetails().contract());
			assertEquals("NYSE", broker.getExchangeName());
		}
	}

	@Test
	void testInitContractNoContractDetailsFound() {
		String ticker = "AAPL";
		String exchangeName = "NASDAQ";
		Trigger disconnectionListener = mock(Trigger.class);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(Collections.emptyList());

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory = Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			assertThrows(BrokerException.class, () -> new IbkrBroker(ticker, exchangeName, disconnectionListener),
			             "Expected BrokerException to be thrown due to no contract details found.");
		}
	}

	@Test
	void testInitContractMultipleContractDetailsFound() {
		String ticker = "AAPL";
		String exchangeName = "NASDAQ";
		Trigger disconnectionListener = mock(Trigger.class);

		ContractDetails contractDetails1 = mock(ContractDetails.class);
		ContractDetails contractDetails2 = mock(ContractDetails.class);
		List<ContractDetails> multipleContractDetails = List.of(contractDetails1, contractDetails2);

		IbkrConnectionHandler ibkrConnectionHandler = mock(IbkrConnectionHandler.class);
		when(ibkrConnectionHandler.lookupContract(any(Contract.class))).thenReturn(multipleContractDetails);

		try (MockedStatic<IbkrConnectionHandlerFactory> mockedFactory = Mockito.mockStatic(IbkrConnectionHandlerFactory.class)) {
			mockedFactory.when(IbkrConnectionHandlerFactory::getConnectionHandler).thenReturn(ibkrConnectionHandler);

			assertThrows(BrokerException.class, () -> new IbkrBroker(ticker, exchangeName, disconnectionListener),
			             "Expected BrokerException to be thrown due to multiple contract details found.");
		}
	}
}

