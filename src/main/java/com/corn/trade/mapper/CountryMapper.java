package com.corn.trade.mapper;

import com.corn.trade.dto.CountryDTO;
import com.corn.trade.entity.Country;

public class CountryMapper {
	public static CountryDTO toDTO(Country entity) {
		return new CountryDTO(entity.getId(), entity.getName());
	}

	public static Country toEntity(CountryDTO dto) {
		return new Country(dto.getName());
	}
}
