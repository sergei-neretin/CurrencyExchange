package com.sergeineretin.converters;

import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.model.Currency;
import com.sergeineretin.model.Exchange;

public class ExchangeConverter {
    public ExchangeDto convertToDto(Exchange entity) {
        CurrencyDto baseCurrencyDto = CurrencyConverter.convertToDto(entity.getBaseCurrency());
        CurrencyDto targetCurrencyDto = CurrencyConverter.convertToDto(entity.getTargetCurrency());
        return new ExchangeDto(
                baseCurrencyDto,
                targetCurrencyDto,
                entity.getRate(),
                entity.getAmount(),
                entity.getConvertedAmount());
    }

    public Exchange convertToEntity(ExchangeDto dto) {
        Currency baseCurrency = CurrencyConverter.convertToEntity(dto.getBaseCurrency());
        Currency targetCurrency = CurrencyConverter.convertToEntity(dto.getTargetCurrency());
        return new Exchange(
                baseCurrency,
                targetCurrency,
                dto.getRate(),
                dto.getAmount(),
                dto.getConvertedAmount());
    }
}
