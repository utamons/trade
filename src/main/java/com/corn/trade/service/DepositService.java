package com.corn.trade.service;

import com.corn.trade.dto.DepositOutDTO;
import com.corn.trade.entity.Broker;
import com.corn.trade.entity.Currency;
import com.corn.trade.entity.Deposit;
import com.corn.trade.mapper.DepositMapper;
import com.corn.trade.repository.BrokerRepository;
import com.corn.trade.repository.CurrencyRepository;
import com.corn.trade.repository.DepositRepository;
import org.springframework.stereotype.Service;

@Service
public class DepositService {
	private final DepositRepository depositRepository;
	private final CurrencyRepository currencyRepository;
	private final BrokerRepository brokerRepository;

	public DepositService(DepositRepository depositRepository, CurrencyRepository currencyRepository,
	                      BrokerRepository brokerRepository) {
		this.depositRepository = depositRepository;
		this.currencyRepository = currencyRepository;
		this.brokerRepository = brokerRepository;
	}

	public DepositOutDTO lastDeposit(long currencyId, long brokerId) {
		Currency currency = currencyRepository.getReferenceById(currencyId);
		Broker broker = brokerRepository.getReferenceById(brokerId);
		Long maxId = depositRepository.getMaxId(currency, broker);
		Deposit deposit = depositRepository.getReferenceById(maxId);
		return DepositMapper.toOutDTO(deposit);
	}
}
