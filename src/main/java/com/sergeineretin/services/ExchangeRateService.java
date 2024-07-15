package com.sergeineretin.services;

import com.sergeineretin.converters.CurrencyConverter;
import com.sergeineretin.converters.ExchangeRateConverter;
import com.sergeineretin.dao.ExchangeRateDao;
import com.sergeineretin.dto.ExchangeRateDto;
import com.sergeineretin.exceptions.ExchangeRateException;
import com.sergeineretin.model.ExchangeRate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public List<ExchangeRateDto> findAll() {
        List<ExchangeRate> entities = exchangeRateDao.findAll();
        return entities.stream().map(e -> new ExchangeRateDto(
                e.getId(),
                CurrencyConverter.convertToDto(e.getBaseCurrency()),
                CurrencyConverter.convertToDto(e.getTargetCurrency()),
                e.getRate())).collect(Collectors.toList());
    }

    public ExchangeRateDto findByCodes(String baseCode, String targetCode) {
        Optional<ExchangeRate> result = exchangeRateDao.findByCodes(baseCode, targetCode);
        if (result.isPresent()) {
            return ExchangeRateConverter.convertToDto(result.get());
        } else {
            throw new ExchangeRateException("Exchange rate not found");
        }
    }

    public ExchangeRateDto create(ExchangeRateDto exchangeRateDto) {
        ExchangeRate exchangeRate = ExchangeRateConverter.convertToEntity(exchangeRateDto);
        exchangeRateDao.create(exchangeRate);
        Optional<ExchangeRate> entity = exchangeRateDao.findByCodes(
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getTargetCurrency().getCode());
        return ExchangeRateConverter.convertToDto(entity.orElse(new ExchangeRate()));
    }

    public ExchangeRateDto update(ExchangeRateDto exchangeRateDto) {
        ExchangeRate exchangeRate = ExchangeRateConverter.convertToEntity(exchangeRateDto);
        Optional<ExchangeRate> optional =  exchangeRateDao.update(exchangeRate);
        return ExchangeRateConverter.convertToDto(optional.orElse(new ExchangeRate()));
    }
}
