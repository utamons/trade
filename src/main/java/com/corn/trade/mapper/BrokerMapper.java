package com.corn.trade.mapper;

import com.corn.trade.dto.BrokerDTO;
import com.corn.trade.entity.Broker;

import java.util.List;
import java.util.stream.Collectors;

public class BrokerMapper {

	public static List<BrokerDTO> toDTOList(List<Broker> entityList) {
		return entityList.stream().map(BrokerMapper::toDTO).collect(Collectors.toList());
	}

	public static BrokerDTO toDTO(Broker entity) {
		return new BrokerDTO(entity.getId(), entity.getName(), entity.getShortName());
	}

	public static Broker toEntity(BrokerDTO dto) {
		return new Broker(dto.getName(), dto.getShortName());
	}
}
