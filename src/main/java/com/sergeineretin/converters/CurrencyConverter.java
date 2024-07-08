package com.sergeineretin.converters;

import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.model.Currency;

public class CurrencyConverter {
    public static CurrencyDto convertToDto(Currency entity) {
        return CurrencyDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .fullName(entity.getFullName())
                .sign(entity.getSign())
                .build();
    }

    public static Currency convertToEntity(CurrencyDto dto) {
        return Currency.builder()
                .id(dto.getId())
                .code(dto.getCode())
                .fullName(dto.getFullName())
                .sign(dto.getSign())
                .build();
    }
}
