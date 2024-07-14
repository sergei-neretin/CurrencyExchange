package com.sergeineretin.services;

import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.exceptions.CurrencyException;
import com.sergeineretin.exceptions.DatabaseException;
import com.sergeineretin.dao.CurrencyDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.model.Currency;
import com.sergeineretin.model.NullCurrency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;
    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public static Currency convertResultSet(ResultSet rs) throws SQLException {
        return Currency.builder()
                .id(rs.getLong("Id"))
                .code(rs.getString("Code"))
                .fullName(rs.getString("FullName"))
                .sign(rs.getString("Sign"))
                .build();
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
        Currency entity = currencyDao.findByName(currencyDto.getCode());
        return CurrencyConverter.convertToDto(entity);
    }

    public CurrencyDto findByName(String code) {
        Currency entity = currencyDao.findByName(code);
        if (!(entity instanceof NullCurrency)) {
            return CurrencyConverter.convertToDto(entity);
        } else {
            throw new CurrencyException("Currency not found", new Throwable());
        }
    }
}
