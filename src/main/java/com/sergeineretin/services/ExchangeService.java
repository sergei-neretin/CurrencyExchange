package com.sergeineretin.services;

import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeService {
    ExchangeRateDao exchangeRateDao;
    public ExchangeService() {
        exchangeRateDao = new ExchangeRateDao();
    }
    public ExchangeDto get(ExchangeDto exchangeDto) {
        String baseCode = exchangeDto.getBaseCurrency().getCode();
        String targetCode = exchangeDto.getTargetCurrency().getCode();

        ExchangeRate result = exchangeRateDao.findByName(baseCode, targetCode);
        if (result == null) {
            result = getInverse(baseCode, targetCode);
        }
        if (result == null) {
            result = getTransitive(baseCode, targetCode);
        }
        if (result != null) {


            return new ExchangeDto(
                    CurrencyConverter.convertToDto(result.getBaseCurrency()),
                    CurrencyConverter.convertToDto(result.getTargetCurrency()),
                    result.getRate(),
                    exchangeDto.getAmount(),
                    result.getRate().multiply( exchangeDto.getAmount()));
        } else {
            throw new ExchangeRateException("Currency pair is absent in the database", new Throwable());
        }
    }

    private ExchangeRate getInverse(String baseCode, String targetCode) {
        ExchangeRate result = exchangeRateDao.findByName(targetCode, baseCode);
        if (result != null) {
            return new ExchangeRate(
                    result.getID(),
                    result.getTargetCurrency(),
                    result.getBaseCurrency(),
                    BigDecimal.ONE.divide(result.getRate(), 2, RoundingMode.HALF_UP));
        } else {
            return null;
        }
    }
    private ExchangeRate getTransitive(String baseCode, String targetCode) {
        ExchangeRate exchangeRate1 = exchangeRateDao.findByName("USD", baseCode);
        ExchangeRate exchangeRate2 = exchangeRateDao.findByName("USD", targetCode);
        if (exchangeRate1 != null && exchangeRate2 != null) {
            return ExchangeRate.builder()
                    .baseCurrency(exchangeRate1.getTargetCurrency())
                    .targetCurrency(exchangeRate2.getTargetCurrency())
                    .rate(exchangeRate2.getRate().divide(exchangeRate1.getRate(),2, RoundingMode.HALF_UP))
                    .build();
        } else {
            return null;
        }
    }
}
