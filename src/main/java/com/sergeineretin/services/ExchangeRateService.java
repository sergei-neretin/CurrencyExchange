package com.sergeineretin.services;

import com.sergeineretin.dao.CurrencyDao;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeRateDto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateService {
    public static ExchangeRateDto convertResultSet(ResultSet rs) throws SQLException {
        CurrencyDao currencyDao = new CurrencyDao();
        if (rs.next()) {
            Long baseID = rs.getLong("baseCurrencyID");
            Long targetID = rs.getLong("TargetCurrencyID");
            CurrencyDto baseCurrency = currencyDao.findById(baseID);
            CurrencyDto targetCurrency = currencyDao.findById(targetID);
            System.out.println(baseCurrency);
            System.out.println(targetCurrency);
            return ExchangeRateDto.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .rate(rs.getBigDecimal("Rate"))
                    .ID(rs.getInt("ID"))
                    .build();
        } else {
            return new ExchangeRateDto();
        }
    }
}
