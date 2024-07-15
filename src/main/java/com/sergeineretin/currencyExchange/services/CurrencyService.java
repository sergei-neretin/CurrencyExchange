package com.sergeineretin.currencyExchange.services;

import com.sergeineretin.currencyExchange.converters.CurrencyConverter;
import com.sergeineretin.currencyExchange.dao.CurrencyDao;
import com.sergeineretin.currencyExchange.dto.CurrencyDto;
import com.sergeineretin.currencyExchange.exceptions.CurrencyException;
import com.sergeineretin.currencyExchange.exceptions.DatabaseException;
import com.sergeineretin.currencyExchange.model.Currency;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;
    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<CurrencyDto> findAll() throws DatabaseException {
        List<Currency> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(c -> new CurrencyDto(c.getId(), c.getCode(), c.getFullName(), c.getSign()))
                .collect(Collectors.toList());
    }

    public CurrencyDto create(CurrencyDto currencyDto) {
        Currency currency = CurrencyConverter.convertToEntity(currencyDto);
        currencyDao.create(currency);
        Currency entity = currencyDao.findByCode(currencyDto.getCode()).orElse(new Currency());
        return CurrencyConverter.convertToDto(entity);
    }

    public CurrencyDto findByCode(String code) {
        Optional<Currency> optional = currencyDao.findByCode(code);
        if (optional.isPresent()) {
            return CurrencyConverter.convertToDto(optional.get());
        } else {
            throw new CurrencyException("Currency not found");
        }
    }
}
