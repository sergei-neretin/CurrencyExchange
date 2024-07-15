package com.sergeineretin.currencyExchange.converters;

import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.dto.ExchangeDto;
import com.sergeineretin.currencyExchange.dto.ExchangeRateDto;
import com.sergeineretin.currencyExchange.model.Currency;
import com.sergeineretin.currencyExchange.model.ExchangeRate;

public class ExchangeRateConverter {
    public static ExchangeRateDto convertToDto(ExchangeRate entity) {
        CurrencyDto baseCurrencyDto = CurrencyConverter.convertToDto(entity.getBaseCurrency());
        CurrencyDto targetCurrencyDto = CurrencyConverter.convertToDto(entity.getTargetCurrency());
        return new ExchangeRateDto(entity.getId(), baseCurrencyDto, targetCurrencyDto, entity.getRate());
    }

    public static ExchangeRate convertToEntity(ExchangeRateDto dto) {
        Currency baseCurrency = CurrencyConverter.convertToEntity(dto.getBaseCurrency());
        Currency targetCurrency = CurrencyConverter.convertToEntity(dto.getTargetCurrency());
        return new ExchangeRate(dto.getId(), baseCurrency, targetCurrency, dto.getRate());
    }

    public static ExchangeRate convertToEntity(ExchangeDto dto) {
        Currency baseCurrency = CurrencyConverter.convertToEntity(dto.getBaseCurrency());
        Currency targetCurrency = CurrencyConverter.convertToEntity(dto.getTargetCurrency());
        return ExchangeRate.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .build();
    }
}
