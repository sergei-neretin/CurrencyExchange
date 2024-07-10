package com.sergeineretin.services;

import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.converters.ExchangeRateConverter;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.model.Currency;
import com.sergeineretin.model.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }
    public static ExchangeRate convertResultSet(ResultSet rs) throws SQLException {
        Currency baseCurrency = new Currency(
                rs.getLong("BaseID"),
                rs.getString("BaseCode"),
                rs.getString("BaseFullName"),
                rs.getString("BaseSign"));
        Currency targetCurrency = new Currency(
                rs.getLong("TargetID"),
                rs.getString("TargetCode"),
                rs.getString("TargetFullName"),
                rs.getString("TargetSign"));

        return ExchangeRate.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .rate(rs.getBigDecimal("Rate"))
                .ID(rs.getInt("ID"))
                .build();
    }

    public List<ExchangeRateDto> findAll() {
        List<ExchangeRate> entities = exchangeRateDao.findAll();
        return entities.stream().map(e -> new ExchangeRateDto(
                e.getID(),
                CurrencyConverter.convertToDto(e.getBaseCurrency()),
                CurrencyConverter.convertToDto(e.getTargetCurrency()),
                e.getRate())).collect(Collectors.toList());
    }

    public ExchangeRateDto findByName(String baseCode, String targetCode) {
        ExchangeRate result = exchangeRateDao.findByName(baseCode, targetCode);
        if (result == null) {
            throw new ExchangeRateException("Exchange rate not found", new Throwable());
        }
        return ExchangeRateConverter.convertToDto(result);
    }

    public ExchangeRateDto create(ExchangeRateDto exchangeRateDto) {
        ExchangeRate exchangeRate = ExchangeRateConverter.convertToEntity(exchangeRateDto);
        exchangeRateDao.create(exchangeRate);
        ExchangeRate entity = exchangeRateDao.findByName(
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getTargetCurrency().getCode());
        return ExchangeRateConverter.convertToDto(entity);
    }

    public ExchangeRateDto update(ExchangeRateDto exchangeRateDto) {
        ExchangeRate exchangeRate = ExchangeRateConverter.convertToEntity(exchangeRateDto);
        String baseCode = exchangeRate.getBaseCurrency().getCode();
        String targetCode = exchangeRate.getTargetCurrency().getCode();
        if (exchangeRateDao.findByName(baseCode, targetCode) != null) {
            exchangeRateDao.update(exchangeRate);
            ExchangeRate entity = exchangeRateDao.findByName(baseCode, targetCode);
            return ExchangeRateConverter.convertToDto(entity);
        } else {
            throw new ExchangeRateException("Currency pair is absent in the database", new Throwable());
        }
    }
}
