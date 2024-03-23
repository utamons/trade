package com.corn.trade.service;

import com.corn.trade.broker.Broker;
import com.corn.trade.entity.Asset;
import com.corn.trade.entity.Exchange;
import com.corn.trade.jpa.AssetRepo;
import com.corn.trade.jpa.DBException;
import com.corn.trade.jpa.ExchangeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AssetServiceTest {

	private AssetService assetService;

	@Mock
	private ExchangeRepo exchangeRepo;

	@Mock
	private AssetRepo assetRepo;

	@Mock
	private Broker broker;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		assetService = new AssetService(exchangeRepo, assetRepo);
	}

	@Test
	void testGetExchangesReturnsSortedExchanges() {
		Exchange exchange1 = new Exchange();
		exchange1.setName("NYSE");
		Exchange exchange2 = new Exchange();
		exchange2.setName("NASDAQ");
		List<Exchange> mockedExchanges = Arrays.asList(exchange2, exchange1); // Unsorted list

		when(exchangeRepo.findAll()).thenReturn(mockedExchanges);

		List<Exchange> result = assetService.getExchanges();

		verify(exchangeRepo, times(1)).findAll();

		assertEquals(2, result.size(), "Should return two exchanges");
		assertEquals("NASDAQ", result.get(0).getName(), "First exchange should be NASDAQ");
		assertEquals("NYSE", result.get(1).getName(), "Second exchange should be NYSE");
	}

	@Test
	void testGetAssetsReturnsSortedAssets() {
		Asset asset1 = new Asset();
		asset1.setName("Asset1");
		Asset asset2 = new Asset();
		asset2.setName("Asset2");
		List<Asset> mockedAssets = Arrays.asList(asset2, asset1); // Unsorted list

		when(assetRepo.findAll()).thenReturn(mockedAssets);

		List<Asset> result = assetService.getAssets();

		verify(assetRepo, times(1)).findAll();

		assertEquals(2, result.size());
		assertEquals("Asset1", result.get(0).getName());
		assertEquals("Asset2", result.get(1).getName());
	}

	@Test
	void testGetAssetNamesReturnsSortedAssetNames() {
		Asset asset1 = new Asset();
		asset1.setName("Asset1");
		Asset asset2 = new Asset();
		asset2.setName("Asset2");
		when(assetRepo.findAll()).thenReturn(Arrays.asList(asset2, asset1));

		List<String> result = assetService.getAssetNames();

		verify(assetRepo, times(1)).findAll();

		assertEquals(Arrays.asList("Asset1", "Asset2"), result);
	}

	@Test
	void testGetExchangeNamesReturnsSortedExchangeNames() {
		Exchange exchange1 = new Exchange();
		exchange1.setName("Exchange1");
		Exchange exchange2 = new Exchange();
		exchange2.setName("Exchange2");
		when(exchangeRepo.findAll()).thenReturn(Arrays.asList(exchange2, exchange1));

		List<String> result = assetService.getExchangeNames();

		verify(exchangeRepo, times(1)).findAll();

		assertEquals(Arrays.asList("Exchange1", "Exchange2"), result);
	}

	@Test
	void testGetExchangeReturnsExchange() throws DBException {
		String exchangeName = "Exchange1";
		Exchange exchange = new Exchange();
		exchange.setName(exchangeName);
		when(exchangeRepo.findExchange(exchangeName)).thenReturn(Optional.of(exchange));

		Exchange result = assetService.getExchange(exchangeName);

		verify(exchangeRepo, times(1)).findExchange(exchangeName);
		assertEquals(exchangeName, result.getName());
	}

	@Test
	void testGetExchangeThrowsDBExceptionWhenNotFound() throws DBException {
		String exchangeName = "NonExistentExchange";
		when(exchangeRepo.findExchange(exchangeName)).thenReturn(Optional.empty());

		assertThrows(DBException.class, () -> assetService.getExchange(exchangeName));

		verify(exchangeRepo, times(1)).findExchange(exchangeName);
	}

	@Test
	void testGetAssetWhenAssetAndExchangeAreFound() throws Exception {
		// Setup
		String assetName = "TestAsset";
		String exchangeName = "TestExchange";
		Exchange exchange = new Exchange();
		exchange.setName(exchangeName);
		Asset expectedAsset = new Asset();
		expectedAsset.setName(assetName);
		expectedAsset.setExchange(exchange);

		when(exchangeRepo.findExchange(exchangeName)).thenReturn(Optional.of(exchange));
		when(assetRepo.findAsset(assetName, exchange)).thenReturn(Optional.of(expectedAsset));

		// Execute
		Asset result = assetService.getAsset(assetName, exchangeName, broker);

		// Verify
		assertEquals(expectedAsset, result);
		verify(exchangeRepo).findExchange(exchangeName);
		verify(assetRepo).findAsset(assetName, exchange);
	}

	@Test
	void testGetAssetWhenAssetIsNotFound() throws Exception {
		// Setup
		String assetName = "TestAsset";
		String exchangeName = "TestExchange";
		Exchange exchange = new Exchange();
		exchange.setName(exchangeName);

		when(exchangeRepo.findExchange(exchangeName)).thenReturn(Optional.of(exchange));
		when(assetRepo.findAsset(assetName, exchange)).thenReturn(Optional.empty());

		when(broker.getExchangeName()).thenReturn(exchangeName);

		assetService.getAsset(assetName, exchangeName, broker);

		Asset newAsset = new Asset();
		newAsset.setName(assetName);
		newAsset.setExchange(exchange);

		verify(exchangeRepo, times(2)).findExchange(exchangeName);
		verify(assetRepo,times(2)).findAsset(assetName, exchange);
		verify(assetRepo).save(eq(newAsset));
	}
}
