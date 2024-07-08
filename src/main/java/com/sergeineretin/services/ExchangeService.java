package com.sergeineretin.services;

import com.sergeineretin.ExchangeRateException;
import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.converters.ExchangeRateConverter;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.model.Currency;
import com.sergeineretin.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeService {
    ExchangeRateDao exchangeRateDao;
    public ExchangeService() {
        exchangeRateDao = new ExchangeRateDao();
    }
    public ExchangeDto get(ExchangeDto exchangeDto) {
        ExchangeRate exchangeRate = ExchangeRateConverter.convertToEntity(exchangeDto);
        ExchangeRate result = exchangeRateDao.findByName(exchangeRate);
        if (result == null) {
            result = getInverse(exchangeRate);
        }
        if (result == null) {
            result = getTransitive(exchangeRate);
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

    private ExchangeRate getInverse(ExchangeRate exchangeRate) {
        ExchangeRate inverseExchangeRate = ExchangeRate.builder()
                .baseCurrency(exchangeRate.getTargetCurrency())
                .targetCurrency(exchangeRate.getBaseCurrency()).build();
        ExchangeRate result = exchangeRateDao.findByName(inverseExchangeRate);
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
    private ExchangeRate getTransitive(ExchangeRate exchangeRate) {
        ExchangeRate exchangeRate1 = exchangeRateDao.findByName(getUSDBasedExchangeRate(exchangeRate.getBaseCurrency()));
        ExchangeRate exchangeRate2 = exchangeRateDao.findByName(getUSDBasedExchangeRate(exchangeRate.getTargetCurrency()));
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
    private ExchangeRate getUSDBasedExchangeRate(Currency targetCurrency) {
        Currency USD = Currency.builder().code("USD").build();
        return ExchangeRate.builder().baseCurrency(USD).targetCurrency(targetCurrency).build();
    }
}
