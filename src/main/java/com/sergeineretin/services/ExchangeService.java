package com.sergeineretin.services;

import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.ExchangeDto;
import com.sergeineretin.exceptions.ExchangeRateException;
import com.sergeineretin.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    ExchangeRateDao exchangeRateDao;
    public ExchangeService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }
    public ExchangeDto get(ExchangeDto exchangeDto) {
        System.out.println(exchangeDto);
        String baseCode = exchangeDto.getBaseCurrency().getCode();
        String targetCode = exchangeDto.getTargetCurrency().getCode();

        Optional<ExchangeRate> optional = exchangeRateDao.findByName(baseCode, targetCode);
        if (!optional.isPresent()) {
            optional = getInverse(baseCode, targetCode);
        }
        if (!optional.isPresent()) {
            optional = getTransitive(baseCode, targetCode);
        }
        if (optional.isPresent()) {
            ExchangeRate exchangeRate = optional.get();
            return new ExchangeDto(
                    CurrencyConverter.convertToDto(exchangeRate.getBaseCurrency()),
                    CurrencyConverter.convertToDto(exchangeRate.getTargetCurrency()),
                    exchangeRate.getRate(),
                    exchangeDto.getAmount(),
                    exchangeRate.getRate().multiply(exchangeDto.getAmount()));
        } else {
            throw new ExchangeRateException("Currency pair is absent in the database");
        }
    }

    private Optional<ExchangeRate> getInverse(String baseCode, String targetCode) {
        Optional<ExchangeRate> optional = exchangeRateDao.findByName(targetCode, baseCode);
        if (optional.isPresent()) {
            ExchangeRate result = optional.get();
            return Optional.of( new ExchangeRate(
                    result.getID(),
                    result.getTargetCurrency(),
                    result.getBaseCurrency(),
                    BigDecimal.ONE.divide(result.getRate(), 2, RoundingMode.HALF_UP)));
        } else {
            return Optional.empty();
        }
    }
    private Optional<ExchangeRate> getTransitive(String baseCode, String targetCode) {
        Optional<ExchangeRate> optional1 = exchangeRateDao.findByName("USD", baseCode);
        Optional<ExchangeRate> optional2 = exchangeRateDao.findByName("USD", targetCode);
        if (optional1.isPresent() && optional2.isPresent()) {
            ExchangeRate exchangeRate1 = optional1.get();
            ExchangeRate exchangeRate2 = optional2.get();
            return Optional.of(
                    ExchangeRate.builder()
                            .baseCurrency(exchangeRate1.getTargetCurrency())
                            .targetCurrency(exchangeRate2.getTargetCurrency())
                            .rate(exchangeRate2.getRate().divide(exchangeRate1.getRate(),2, RoundingMode.HALF_UP))
                            .build()
            );
        } else {
            return Optional.empty();
        }
    }
}
