package com.sergeineretin.dao;

import com.sergeineretin.Utils;
import com.sergeineretin.dto.CurrencyDto;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.dto.ExchangeRateDto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.*;

public class ExchangeDao {
    private final MathContext mc = new MathContext(2, RoundingMode.HALF_EVEN);

    CurrencyDao currencyDao = new CurrencyDao();

    private final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();

    public ExchangeDto read(ExchangeDto exchangeDto) throws Exception {

        Connection conn = DriverManager.getConnection(Utils.getDatabaseUrl());

        Statement stmtBase = conn.createStatement();
        Statement stmtTarget = conn.createStatement();
        ResultSet rsCurrencyBase = stmtBase.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeDto.getBaseCurrency().getCode() + "';");
        ResultSet rsCurrencyTarget = stmtTarget.executeQuery("SELECT * FROM Currencies WHERE Code = " + "'" + exchangeDto.getTargetCurrency().getCode() + "';");

        CurrencyDto baseCurrency = CurrencyDto.builder()
                .id(rsCurrencyBase.getLong("ID"))
                .code(rsCurrencyBase.getString("Code"))
                .fullName(rsCurrencyBase.getString("FullName"))
                .sign(rsCurrencyBase.getString("Sign"))
                .build();

        CurrencyDto targetCurrency = CurrencyDto.builder()
                .id(rsCurrencyTarget.getLong("ID"))
                .code(rsCurrencyTarget.getString("Code"))
                .fullName(rsCurrencyTarget.getString("FullName"))
                .sign(rsCurrencyTarget.getString("Sign"))
                .build();

        BigDecimal amount = exchangeDto.getAmount();

        ExchangeRateDto result = exchangeRateDao.read(
                ExchangeRateDto.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(targetCurrency)
                        .build()
        );


        if (!result.equals(new ExchangeRateDto())) {
            BigDecimal convertedAmount = amount.multiply(result.getRate());
            return new ExchangeDto(
                    baseCurrency,
                    targetCurrency,
                    result.getRate(),
                    exchangeDto.getAmount(),
                    convertedAmount);
        }

        ExchangeRateDto inverseExchangeRateDto = ExchangeRateDto.builder()
                .baseCurrency(targetCurrency)
                .targetCurrency(baseCurrency)
                .build();

        result = exchangeRateDao.read(inverseExchangeRateDto);

        if (!result.equals(new ExchangeRateDto())) {
            BigDecimal rate = BigDecimal.ONE.divide(result.getRate(), RoundingMode.FLOOR);
            BigDecimal convertedAmount = amount.multiply(rate);
            return new ExchangeDto(
                    baseCurrency,
                    targetCurrency,
                    result.getRate(),
                    exchangeDto.getAmount().round(mc),
                    convertedAmount);
        } else {
            CurrencyDto currencyUSD = currencyDao.findByName("USD");

            ExchangeRateDto rate1 = ExchangeRateDto.builder()
                    .baseCurrency(currencyUSD)
                    .targetCurrency(baseCurrency)
                    .build();

            ExchangeRateDto rate2 = ExchangeRateDto.builder()
                    .baseCurrency(currencyUSD)
                    .targetCurrency(targetCurrency)
                    .build();
            if (!(rate1 = exchangeRateDao.read(rate1)).equals(new ExchangeRateDto()) &&
                    (!(rate2 = exchangeRateDao.read(rate2)).equals(new ExchangeRateDto()))) {
                BigDecimal calculatedRate = rate2.getRate().divide(rate1.getRate(), RoundingMode.FLOOR);

                return ExchangeDto.builder()
                        .baseCurrency(baseCurrency)
                        .targetCurrency(targetCurrency)
                        .rate(calculatedRate)
                        .amount(amount)
                        .convertedAmount(calculatedRate.multiply(amount).round(mc))
                        .build();
            } else {
                return new ExchangeDto();
            }
        }
    }
}
